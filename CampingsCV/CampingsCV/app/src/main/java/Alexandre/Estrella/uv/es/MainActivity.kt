package Alexandre.Estrella.uv.es

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import Alexandre.Estrella.uv.es.ui.theme.CampingsCVTheme
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.data.ContextCache
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampingsCVTheme {
                CampingsScreen()
            }
        }
    }
}
@Composable
fun CampingsScreen() {
    val context = LocalContext.current
    val rawData = remember { getData(context) }

    val campings = remember { mutableStateOf(
        rawData.sortedBy { it.nombre } // orden inicial por nombre
    ) }
    var currentSort by remember { mutableStateOf("nombre") }
    var ascending by remember { mutableStateOf(true) }
    var tipo by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    // --- Función local para ordenar ---
    fun applySort() {
        campings.value = when (currentSort) {
            "nombre" -> if (ascending) rawData.sortedBy { it.nombre } else rawData.sortedByDescending { it.nombre }
            "categoria" -> if (ascending) rawData.sortedBy { it.categoria } else rawData.sortedByDescending { it.categoria }
            "municipio" -> if (ascending) rawData.sortedBy { it.municipio } else rawData.sortedByDescending { it.municipio }
            else -> rawData
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- Row con Dropdown y botón Asc/Desc ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {

            // Botón de Dropdown con texto dinámico
            androidx.compose.material3.OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.weight(1f)
            ) {
                val displayText = when (currentSort) {
                    "nombre" -> "Sort by: Name"
                    "categoria" -> "Sort by: Category"
                    "municipio" -> "Sort by: Municipality"
                    else -> "Sort by..."
                }
                Text(displayText)
            }

            // Botón Asc/Desc
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    ascending = !ascending
                    applySort()
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (ascending) "▲" else "▼")
            }

            // DropdownMenu asociado al primer botón
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Name") },
                    onClick = {
                        currentSort = "nombre"
                        applySort()
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Category") },
                    onClick = {
                        currentSort = "categoria"
                        applySort()
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Municipality") },
                    onClick = {
                        currentSort = "municipio"
                        applySort()
                        expanded = false
                    }
                )
            }
        }

        // --- Lista ---
        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 1.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(campings.value) { camping ->
                CampingItem(camping)
            }
        }
    }
}


@Composable
fun CampingItem(camping: Camping) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = camping.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Municipio: ${camping.municipio}")
            Text(text = "Provincia: ${camping.provincia}")
            Text(text = "Categoria: ${camping.categoria}")
            Text(text = "Dirección: ${camping.direccion}")
            Text(text = "Teléfono: ${camping.telefono}")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun CampingPreview(){
    CampingsCVTheme() {
        CampingsScreen()
    }
}
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

        val nombre = campingObject.optString("Nombre", "No disponible")
        val municipio = campingObject.optString("Municipio", "No disponible")
        val provincia = campingObject.optString("Provincia", "No disponible")
        val categoria = campingObject.optString("Categoria","No disponible")
        val direccion = campingObject.optString("Direccion", "No disponible")
        val telefono = campingObject.optString("Telefono", "No disponible")

        listaCampings.add(
            Camping(nombre, municipio, provincia, categoria, direccion, telefono)
        )
    }

    return listaCampings
}





