package com.example.grocersync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    var isDataLoaded by mutableStateOf(false)   // Control para el Splash

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = FirebaseFirestore.getInstance()
        val localDb = AppDatabase.getDatabase(this)
        val syncRepository = SyncRepository(db, localDb.listaDao())

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                cargarUsuariosDesdeJson()
                cargarListasDesdeJson()
                cargarItemsDesdeJson()
                cargarListaUsuarioDesdeJson()  // Ahora carga relaciones correctamente
                syncRepository.fullSync()
            }
            withContext(Dispatchers.Main) {
                isDataLoaded = true
            }
        }

        // Test Firebase
        val test = hashMapOf("mensaje" to "Firebase funciona Estrella")
        db.collection("GrocerSyncDB").add(test)
            .addOnSuccessListener { Log.d("FIREBASE", "FUNCIONA Estrella") }
            .addOnFailureListener { Log.e("FIREBASE", "ERROR") }

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { usuarioId ->
                            usuarioActualId = usuarioId
                            navController.navigate("select") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToSign = { navController.navigate("signup") }
                    )
                }

                composable("signup") {
                    SignScreen(onSignSuccess = { navController.popBackStack() })
                }

                composable("main/{listId}") { backStackEntry ->
                    val context = LocalContext.current
                    val listId = backStackEntry.arguments?.getString("listId")?.toIntOrNull() ?: 1
                    val dao = AppDatabase.getDatabase(context).listaDao()
                    val repository = ListaRepository(
                        dao = dao,
                        db = FirebaseFirestore.getInstance(),
                        context = context
                    )
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
                        onListSelected = { listaId -> navController.navigate("main/$listaId") },
                        onBack = { navController.popBackStack() },
                        onAddClick = { navController.navigate("add_item") },
                        onStatsClick = { navController.navigate("stats") },
                        onCreateListClick = { navController.navigate("addList") },
                        navController = navController  // Pasamos navController para el refresco
                    )
                }

                composable("addItem/{listId}") { backStackEntry ->
                    val listId = backStackEntry.arguments?.getString("listId")?.toIntOrNull() ?: 1
                    val context = LocalContext.current
                    val dao = AppDatabase.getDatabase(context).listaDao()
                    val repository = ListaRepository(
                        dao = dao,
                        db = FirebaseFirestore.getInstance(),
                        context = context
                    )
                    AddItemScreen(
                        repository = repository,
                        listId = listId,
                        onItemAdded = { navController.popBackStack() }   // ← CIERRA LA PANTALLA
                    )
                }

                composable("stats") { StatisticsScreen() }

                composable("addList") {
                    val dao = AppDatabase.getDatabase(this@MainActivity).listaDao()
                    val repository = ListaRepository(
                        dao = dao,
                        db = FirebaseFirestore.getInstance(),
                        context = this@MainActivity
                    )
                    AddListScreen(
                        usuarioId = usuarioActualId,
                        repository = repository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    // ================== CARGA DE DATOS ==================
    private suspend fun cargarUsuariosDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        if (dao.getUsuarios().isNotEmpty()) return
        try {
            val json = resources.openRawResource(R.raw.users).bufferedReader().use { it.readText() }
            val usuarios: List<Usuario> = Gson().fromJson(json, object : TypeToken<List<Usuario>>() {}.type)
            usuarios.forEach { dao.insertUsuario(it) }
            Log.d("DB", "Usuarios cargados: ${usuarios.size}")
        } catch (e: Exception) { Log.e("DB", "Error usuarios", e) }
    }

    private suspend fun cargarListasDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        if (dao.getListas().isNotEmpty()) return
        try {
            val json = resources.openRawResource(R.raw.listas).bufferedReader().use { it.readText() }
            val listas: List<Lista> = Gson().fromJson(json, object : TypeToken<List<Lista>>() {}.type)
            listas.forEach { dao.insertLista(it) }
            Log.d("DB", "Listas cargadas: ${listas.size}")
        } catch (e: Exception) { Log.e("DB", "Error listas", e) }
    }

    private suspend fun cargarItemsDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        if (dao.getAllItems().isNotEmpty()) return
        try {
            val json = resources.openRawResource(R.raw.items).bufferedReader().use { it.readText() }
            val items: List<Item> = Gson().fromJson(json, object : TypeToken<List<Item>>() {}.type)
            items.forEach { dao.insertItem(it) }
            Log.d("DB", "Items cargados: ${items.size}")
        } catch (e: Exception) { Log.e("DB", "Error items", e) }
    }

    // ✅ Corregido: solo carga si no existen relaciones
    private suspend fun cargarListaUsuarioDesdeJson() {
        val dao = AppDatabase.getDatabase(applicationContext).listaDao()
        if (dao.getCountListaUsuarioCrossRef() > 0) return
        try {
            val json = resources.openRawResource(R.raw.listusers).bufferedReader().use { it.readText() }
            val relaciones: List<ListaUsuarioCrossRef> = Gson().fromJson(json, object : TypeToken<List<ListaUsuarioCrossRef>>() {}.type)
            relaciones.forEach { dao.insertListaUsuarioCrossRef(it) }
            Log.d("DB", "Relaciones cargadas: ${relaciones.size}")
        } catch (e: Exception) { Log.e("DB", "Error relaciones", e) }
    }
}