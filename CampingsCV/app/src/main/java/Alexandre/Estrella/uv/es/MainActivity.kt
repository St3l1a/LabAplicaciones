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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import org.json.JSONObject
import Alexandre.Estrella.uv.es.ui.theme.CampingsCVTheme
import androidx.navigation.navArgument

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
            arguments = listOf(navArgument(Routes.ARG_ID) {
                type = NavType.StringType
            })
        ) { backStackEntry ->

            val campingId = backStackEntry.arguments?.getString(Routes.ARG_ID)
            val selectedCamping = campings.firstOrNull { it.id == campingId }

            CampingDetailScreen(
                camping = selectedCamping,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// -------------------- LIST SCREEN --------------------

@Composable
fun CampingsListScreen(
    campings: List<Camping>,
    onCampingClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(campings) { camping ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = { onCampingClick(camping.id) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = camping.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Municipio: ${camping.municipio}")
                    Text("Provincia: ${camping.provincia}")
                }
            }
        }
    }
}

// -------------------- DETAIL SCREEN --------------------

@Composable
fun CampingDetailScreen(
    camping: Camping?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(camping?.nombre ?: "Camping Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (camping == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text("Camping not found", modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

// -------------------- JSON FUNCTIONS --------------------

fun readJsonFromRaw(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return inputStream.bufferedReader().use { it.readText() }
}

fun getData(context: Context): List<Camping> {

    val listaCampings = mutableListOf<Camping>()
    val rawResourceId = R.raw.datos

    val jsonFileContent = readJsonFromRaw(context, rawResourceId)

    val rootObject = JSONObject(jsonFileContent)
    val resultObject = rootObject.getJSONObject("result")
    val jsonArray = resultObject.getJSONArray("records")

    for (i in 0 until jsonArray.length()) {

        val campingObject = jsonArray.getJSONObject(i)

        val id = campingObject.optString("_id", i.toString())
        val nombre = campingObject.optString("Nombre", "No disponible")
        val municipio = campingObject.optString("Municipio", "No disponible")
        val provincia = campingObject.optString("Provincia", "No disponible")
        val categoria = campingObject.optString("Categoria", "No disponible")
        val direccion = campingObject.optString("Direccion", "No disponible")
        val email = campingObject.optString("Email", "No disponible")

        listaCampings.add(
            Camping(id, nombre, municipio, provincia, categoria, direccion, email)
        )
    }

    return listaCampings
}