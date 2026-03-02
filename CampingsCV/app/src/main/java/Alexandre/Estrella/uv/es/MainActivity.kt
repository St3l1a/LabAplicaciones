package Alexandre.Estrella.uv.es

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

// -------------------- MODELO Y ENUMS --------------------

enum class SortOption(val label: String) {
    NOMBRE("Nombre"), MUNICIPIO("Municipio"), CATEGORIA("Categoría")
}

// -------------------- RUTAS --------------------

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{id}"
    fun detailRoute(id: String) = "detail/$id"
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

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            CampingsListScreen(
                campings = campings,
                onCampingClick = { id -> navController.navigate(Routes.detailRoute(id)) }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val camping = campings.find { it.id == id }
            CampingDetailScreen(camping = camping, onBack = { navController.popBackStack() })
        }
    }
}

// -------------------- LIST SCREEN (CON BUSQUEDA Y FILTROS) --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampingsListScreen(campings: List<Camping>, onCampingClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var currentSort by remember { mutableStateOf(SortOption.NOMBRE) }
    var isAscending by remember { mutableStateOf(true) }
    var selectedProvincia by remember { mutableStateOf("Todas") }

    val provincias = listOf("Todas") + campings.map { it.provincia }.distinct().sorted()

    // Lógica combinada: Filtrado + Búsqueda + Ordenación
    val filteredList = remember(searchQuery, currentSort, isAscending, selectedProvincia) {
        campings
            .filter {
                (it.nombre.contains(searchQuery, ignoreCase = true) || it.municipio.contains(searchQuery, ignoreCase = true)) &&
                        (selectedProvincia == "Todas" || it.provincia == selectedProvincia)
            }
            .let { list ->
                val selector: (Camping) -> String = when (currentSort) {
                    SortOption.NOMBRE -> { { it.nombre } }
                    SortOption.MUNICIPIO -> { { it.municipio } }
                    SortOption.CATEGORIA -> { { it.categoria } }
                }
                if (isAscending) list.sortedBy(selector) else list.sortedByDescending(selector)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campings CV") },
                actions = {
                    // Botón para alternar orden A-Z / Z-A
                    IconButton(onClick = { isAscending = !isAscending }) {
                        Icon(if (isAscending) Icons.Default.SortByAlpha else Icons.Default.VerticalAlignBottom, "Orden")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // BUSCADOR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar camping o municipio...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // FILTRO POR PROVINCIA (Chips)
            Text("Filtrar por provincia:", modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.labelSmall)
            LazyColumn(modifier = Modifier.height(50.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                item {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        provincias.forEach { prov ->
                            FilterChip(
                                selected = selectedProvincia == prov,
                                onClick = { selectedProvincia = prov },
                                label = { Text(prov) }
                            )
                        }
                    }
                }
            }

            // SELECTOR DE CRITERIO DE ORDEN
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Ordenar por: ", style = MaterialTheme.typography.labelSmall)
                SortOption.entries.forEach { option ->
                    TextButton(onClick = { currentSort = option }) {
                        Text(option.label, color = if(currentSort == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // LISTA
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList) { camping ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        onClick = { onCampingClick(camping.id) }
                    ) {
                        ListItem(
                            headlineContent = { Text(camping.nombre) },
                            supportingContent = { Text("${camping.municipio} (${camping.provincia})") },
                            trailingContent = {
                                StarRating(getStarCount(camping.codCategoria))
                            }
                        )
                    }
                }
            }
        }
    }
}
fun getStarCount(codCategoria: String): Int {
    return codCategoria.firstOrNull()?.digitToIntOrNull() ?: 0
}
@Composable
fun StarRating(stars: Int) {
    Row {
        repeat(stars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
// -------------------- DETAIL SCREEN (CON INTENTS) --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampingDetailScreen(camping: Camping?, onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(camping?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") }
                },
                actions = {
                    // Acción: Abrir Mapas
                    IconButton(onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${camping?.nombre}, ${camping?.municipio}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }) {
                        Icon(Icons.Default.Map, "Ver en Mapa")
                    }
                    // Acción: Abrir Web (Simulada con búsqueda si no hay URL directa)
                    IconButton(
                        enabled = !camping?.web.isNullOrBlank(),
                        onClick = {
                            camping?.web?.let { url ->
                                val finalUrl = if (url.startsWith("http")) url else "https://$url"
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                                context.startActivity(webIntent)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Public, "Web")
                    }
                }
            )
        }
    ) { padding ->
        camping?.let {
            Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Sección Info Principal
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        DetailItem(Icons.Default.Badge, "Signatura", it.signatura)
                        DetailItem(Icons.Default.Info, "Estado", it.estado)
                        DetailItem(Icons.Default.Star, "Categoría", it.categoria)
                        DetailItem(Icons.Default.Home, "Modalidad", it.modalidad)

                        DetailItem(Icons.Default.LocationOn, "Provincia", it.provincia)
                        DetailItem(Icons.Default.Place, "Municipio", it.municipio)
                        DetailItem(Icons.Default.LocationCity, "Dirección", it.direccion)
                        DetailItem(Icons.Default.Numbers, "CP", it.cp)

                        DetailItem(Icons.Default.Email, "Email", it.email)
                        DetailItem(Icons.Default.Public, "Web", it.web)

                        DetailItem(Icons.Default.Terrain, "Parcelas", it.numParcelas)
                        DetailItem(Icons.Default.Group, "Plazas parcela", it.plazasParcela)
                        DetailItem(Icons.Default.HolidayVillage, "Bungalows", it.numBungalows)
                        DetailItem(Icons.Default.GroupWork, "Plazas bungalows", it.plazasBungalows)

                        DetailItem(Icons.Default.CalendarToday, "Fecha Alta", it.fechaAlta)
                        DetailItem(Icons.Default.EventBusy, "Fecha Baja", it.fechaBaja)
                        DetailItem(Icons.Default.Schedule, "Periodo", it.periodo)

                        DetailItem(Icons.Default.Groups, "Plazas Totales", it.plazas)
                    }
                }

                Text(
                    "Utiliza los iconos de la barra superior para navegar hasta el camping o visitar su sitio web oficial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// -------------------- FUNCIONES DE DATOS --------------------

fun getData(context: Context): List<Camping> {
    val list = mutableListOf<Camping>()
    try {
        val json = context.resources.openRawResource(R.raw.datos)
            .bufferedReader()
            .use { it.readText() }

        val records = JSONObject(json)
            .getJSONObject("result")
            .getJSONArray("records")

        for (i in 0 until records.length()) {
            val o = records.getJSONObject(i)

            list.add(
                Camping(
                    id = o.optString("_id"),
                    signatura = o.optString("Signatura"),
                    codEstado = o.optString("Cod.Estado"),
                    estado = o.optString("Estado"),
                    codCategoria = o.optString("Cod. Categoria"),
                    categoria = o.optString("Categoria"),
                    nombre = o.optString("Nombre"),
                    codProvincia = o.optString("Cod. Provincia"),
                    provincia = o.optString("Provincia"),
                    codMunicipio = o.optString("Cod. Municipio"),
                    municipio = o.optString("Municipio"),
                    cp = o.optString("CP"),
                    direccion = o.optString("Direccion"),
                    codTipoVia = o.optString("Cod. Tipo Via"),
                    tipoVia = o.optString("Tipo via"),
                    via = o.optString("Via"),
                    numero = o.optString("Numero"),
                    email = o.optString("Email"),
                    web = o.optString("Web"),
                    codModalidad = o.optString("Cod. Modalidad"),
                    modalidad = o.optString("Modalidad"),
                    numParcelas = o.optString("Núm. Parcelas"),
                    plazasParcela = o.optString("Plazas Parcela"),
                    numBungalows = o.optString("Núm. Bungalows"),
                    plazasBungalows = o.optString("Plaza Bungalows"),
                    supLibreAcampada = o.optString("Sup. Libre Acampada"),
                    plazasLibreAcampada = o.optString("Plazas Libre Acampada"),
                    plazas = o.optString("Plazas"),
                    fechaAlta = o.optString("Fecha Alta"),
                    fechaBaja = o.optString("Fecha Baja"),
                    periodo = o.optString("Periodo"),
                    diasPeriodo = o.optString("Días Periodo")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return list
}