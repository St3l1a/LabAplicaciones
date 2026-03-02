package Alexandre.Estrella.uv.es

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import org.json.JSONObject
import Alexandre.Estrella.uv.es.ui.theme.CampingsCVTheme

// -------------------- ENUM DE ORDENACIÓN --------------------

enum class SortOption(val label: String) {
    NOMBRE("Nombre"),
    MUNICIPIO("Municipio"),
    PROVINCIA("Provincia"),
    CATEGORIA("Categoría")
}

// -------------------- ROUTES --------------------

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail"
    const val ARG_ID = "id"
    const val DETAIL_ROUTE = "$DETAIL/{$ARG_ID}"

    fun detailRoute(id: String) = "$DETAIL/$id"
}

// -------------------- MAIN ACTIVITY --------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampingsCVTheme {
                AppNavGraph()
            }
        }
    }
}

// -------------------- NAV GRAPH --------------------

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val campings = remember { getData(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.LIST
    ) {
        composable(Routes.LIST) {
            CampingsListScreen(
                campings = campings,
                onCampingClick = { campingId ->
                    navController.navigate(Routes.detailRoute(campingId))
                }
            )
        }

        composable(
            route = Routes.DETAIL_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val campingId = backStackEntry.arguments?.getString(Routes.ARG_ID)
            val selectedCamping = campings.find { it.id == campingId }

            CampingDetailScreen(
                camping = selectedCamping,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// -------------------- LIST SCREEN (CON SORT ASC/DESC) --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampingsListScreen(
    campings: List<Camping>,
    onCampingClick: (String) -> Unit
) {
    // Estados para el criterio y la dirección
    var currentSort by remember { mutableStateOf(SortOption.NOMBRE) }
    var isAscending by remember { mutableStateOf(true) }

    // Lógica de ordenación combinada
    val sortedCampings = remember(campings, currentSort, isAscending) {
        val selector: (Camping) -> String = when (currentSort) {
            SortOption.NOMBRE -> { { it.nombre } }
            SortOption.MUNICIPIO -> { { it.municipio } }
            SortOption.PROVINCIA -> { { it.provincia } }
            SortOption.CATEGORIA -> { { it.categoria } }
        }

        if (isAscending) campings.sortedBy(selector)
        else campings.sortedByDescending(selector)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Campings CV") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Cabecera de controles: Texto + Botón de dirección
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ordenar por:",
                    style = MaterialTheme.typography.labelMedium
                )

                // Botón para cambiar entre A-Z y Z-A
                TextButton(onClick = { isAscending = !isAscending }) {
                    Icon(
                        imageVector = if (isAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                    Text(if (isAscending) "A-Z" else "Z-A")
                }
            }

            // Fila de Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = currentSort == option,
                        onClick = { currentSort = option },
                        label = { Text(option.label) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(sortedCampings) { camping ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onCampingClick(camping.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = camping.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Municipio: ${camping.municipio}", style = MaterialTheme.typography.bodySmall)
                            Text("Provincia: ${camping.provincia}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// -------------------- DETAIL SCREEN --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampingDetailScreen(
    camping: Camping?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(camping?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (camping == null) {
            Text("No encontrado", modifier = Modifier.padding(padding).padding(16.dp))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Nombre", camping.nombre)
                DetailRow("Municipio", camping.municipio)
                DetailRow("Provincia", camping.provincia)
                DetailRow("Categoría", camping.categoria)
                DetailRow("Dirección", camping.direccion)
                DetailRow("Email", camping.email)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}

// -------------------- JSON FUNCTIONS --------------------

fun readJsonFromRaw(context: Context, resourceId: Int): String {
    return context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
}

fun getData(context: Context): List<Camping> {
    val listaCampings = mutableListOf<Camping>()
    try {
        val jsonFileContent = readJsonFromRaw(context, R.raw.datos)
        val rootObject = JSONObject(jsonFileContent)
        val resultObject = rootObject.getJSONObject("result")
        val jsonArray = resultObject.getJSONArray("records")

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            listaCampings.add(
                Camping(
                    id = obj.optString("_id", i.toString()),
                    nombre = obj.optString("Nombre", "N/A"),
                    municipio = obj.optString("Municipio", "N/A"),
                    provincia = obj.optString("Provincia", "N/A"),
                    categoria = obj.optString("Categoria", "N/A"),
                    direccion = obj.optString("Direccion", "N/A"),
                    email = obj.optString("Email", "N/A")
                )
            )
        }
    } catch (e: Exception) { e.printStackTrace() }
    return listaCampings
}