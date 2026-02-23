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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
    val campings = remember {getData(context)}
    LazyColumn (
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp))
    {
        items(campings) { camping ->
            CampingItem(camping)
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
fun readJsonFromRaw(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return inputStream.bufferedReader().use { it.readText() }
}
 fun getData(context: Context): List<Camping> {

    val listaCampings = mutableListOf<Camping>()
    val rawResourceId = R.raw.datos

    val jsonFileContent = readJsonFromRaw(context, rawResourceId)

    val jsonObject = JSONObject(jsonFileContent)

    // Get records array
    val jsonArray = JSONArray(jsonFileContent)

    for (i in 0 until jsonArray.length()) {

        val campingObject = jsonArray.getJSONObject(i)

        val nombre = campingObject.optString("NOMBRE", "No disponible")
        val municipio = campingObject.optString("MUNICIPIO", "No disponible")
        val provincia = campingObject.optString("PROVINCIA", "No disponible")
        val categoria = campingObject.optString("CATEGORIA","No disponible")
        val direccion = campingObject.optString("DIRECCION", "No disponible")
        val telefono = campingObject.optString("TELEFONO", "No disponible")



        listaCampings.add(Camping(nombre, municipio, provincia, categoria, direccion, telefono))
    }

    return listaCampings
}





