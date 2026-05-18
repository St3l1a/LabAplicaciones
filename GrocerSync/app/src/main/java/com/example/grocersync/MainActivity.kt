package com.example.grocersync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.grocersync.database.*
import com.example.grocersync.screen.*
import com.example.grocersync.ui.*
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    var usuarioActualId: Int = -1

    // ✔ Firebase bien declarado (GLOBAL)
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // =========================
        // 1. FIREBASE INIT
        // =========================
        db = FirebaseFirestore.getInstance()

        // =========================
        // 2. LOCAL DB + SYNC
        // =========================
        val localDb = AppDatabase.getDatabase(this)
        val syncRepository = SyncRepository(db, localDb.listaDao())

        // =========================
        // 3. LOAD LOCAL DATA FIRST
        // =========================
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {

                cargarUsuariosDesdeJson()
                cargarListasDesdeJson()
                cargarItemsDesdeJson()
                cargarListaUsuarioDesdeJson()

                // ✔ sync SOLO cuando Room ya tiene datos
                syncRepository.fullSync()
            }
        }

        // =========================
        // 4. TEST FIREBASE (OPCIONAL)
        // =========================
        val test = hashMapOf(
            "mensaje" to "Firebase funciona Estrella"
        )

        db.collection("GrocerSyncDB")
            .add(test)
            .addOnSuccessListener {
                Log.d("FIREBASE", "FUNCIONA Estrella")
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "ERROR")
            }

        // =========================
        // 5. UI NAVIGATION
        // =========================
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "login"
            ) {

                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { usuarioId ->
                            usuarioActualId = usuarioId
                            navController.navigate("select") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable("main/{listId}") { backStackEntry ->

                    val context = LocalContext.current

                    val listId = backStackEntry
                        .arguments
                        ?.getString("listId")
                        ?.toIntOrNull() ?: 1

                    val dao = AppDatabase.getDatabase(context).listaDao()
                    val repository = ListaRepository(dao)

                    MainListScreen(
                        listId = listId,
                        repository = repository,
                        onAddClick = { navController.navigate("addItem/$listId") },
                        onStatsClick = { navController.navigate("stats/$listId") }
                    )
                }

                composable("select") {
                    SelectListScreen(
                        usuarioId = usuarioActualId,
                        onListSelected = { listaId ->
                            navController.navigate("main/$listaId")
                        },
                        onBack = { navController.popBackStack() },
                        onAddClick = { navController.navigate("add_item") },
                        onStatsClick = { navController.navigate("stats") }
                    )
                }

                composable("add_item") {
                    AddItemScreen()
                }

                composable("stats") {
                    StatisticsScreen()
                }
            }
        }
    }

    // =====================================================
    // 📥 JSON LOADERS (IGUAL QUE LOS TUYOS PERO OK)
    // =====================================================

    private suspend fun cargarUsuariosDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        dao.deleteAllUsuarios()

        try {
            val json = resources.openRawResource(R.raw.users)
                .bufferedReader().use { it.readText() }

            val usuarios: List<Usuario> = Gson().fromJson(
                json,
                object : TypeToken<List<Usuario>>() {}.type
            )

            usuarios.forEach { dao.insertUsuario(it) }

            Log.d("DB", "Usuarios: ${usuarios.size}")

        } catch (e: Exception) {
            Log.e("DB", "Error usuarios", e)
        }
    }

    private suspend fun cargarListasDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        dao.deleteAllListas()
        dao.deleteAllCrossRefs()

        try {
            val json = resources.openRawResource(R.raw.listas)
                .bufferedReader().use { it.readText() }

            val listas: List<Lista> = Gson().fromJson(
                json,
                object : TypeToken<List<Lista>>() {}.type
            )

            listas.forEach {
                dao.insertLista(it)
                asignarUsuarioALista(dao, it.id, it.idCreador)
            }

            Log.d("DB", "Listas: ${listas.size}")

        } catch (e: Exception) {
            Log.e("DB", "Error listas", e)
        }
    }

    private suspend fun cargarListaUsuarioDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        dao.deleteAllListaUsuarios()

        try {
            val json = resources.openRawResource(R.raw.listusers)
                .bufferedReader().use { it.readText() }

            val relaciones: List<ListaUsuarioCrossRef> = Gson().fromJson(
                json,
                object : TypeToken<List<ListaUsuarioCrossRef>>() {}.type
            )

            relaciones.forEach { dao.insertListaUsuarioCrossRef(it) }

            Log.d("DB", "Relaciones: ${relaciones.size}")

        } catch (e: Exception) {
            Log.e("DB", "Error relaciones", e)
        }
    }

    private suspend fun cargarItemsDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        dao.deleteAllItems()

        try {
            val json = resources.openRawResource(R.raw.items)
                .bufferedReader().use { it.readText() }

            val items: List<Item> = Gson().fromJson(
                json,
                object : TypeToken<List<Item>>() {}.type
            )

            items.forEach { dao.insertItem(it) }

            Log.d("DB", "Items: ${items.size}")

        } catch (e: Exception) {
            Log.e("DB", "Error items", e)
        }
    }

    private suspend fun asignarUsuarioALista(dao: ListaDao, listaId: Int, usuarioId: Int) {
        dao.insertCrossRef(
            ListaUsuarioCrossRef(listaId, usuarioId)
        )
    }
}