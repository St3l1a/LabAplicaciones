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
import com.example.grocersync.database.AppDatabase
import com.example.grocersync.database.Item
import com.example.grocersync.database.Lista
import com.example.grocersync.database.ListaDao
import com.example.grocersync.database.ListaRepository
import com.example.grocersync.database.ListaUsuarioCrossRef
import com.example.grocersync.database.Usuario
import com.example.grocersync.screen.AddItemScreen
import com.example.grocersync.screen.LoginScreen
import com.example.grocersync.screen.SignScreen
import com.example.grocersync.screen.StatisticsScreen
import com.example.grocersync.ui.MainListScreen
import com.example.grocersync.ui.SelectListScreen
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    var usuarioActualId: Int = -1
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = FirebaseFirestore.getInstance()
        insertarEnGrocersyncDB()

        val test = hashMapOf(
            "mensaje" to "Firebase funciona"
        )

        db.collection("test")
            .add(test)
            .addOnSuccessListener {
                Log.d("FIREBASE", "FUNCIONA")
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "ERROR")
            }

// 📥 Cargar usuarios desde JSON si la BD está vacía
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                cargarUsuariosDesdeJson()
                cargarListasDesdeJson()
                cargarItemsDesdeJson()
                cargarListaUsuarioDesdeJson()
            }
        }
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "login"
            ) {

                // 🔵 LOGIN (primera pantalla)
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { usuarioId ->
                            usuarioActualId = usuarioId
                            navController.navigate("select") {
                                popUpTo("login") { inclusive = true } // evita volver atrás al login
                            }
                        },
                        onNavigateToSign = {  // ← NUEVO
                            navController.navigate("sign")
                        }
                    )
                }

                // 🟢 SIGN (pantalla de registro)
                composable("sign") {
                    SignScreen(
                        onSignSuccess = { userId ->
                            navController.navigate("login") {
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

                    val db = AppDatabase.getDatabase(context)
                    val dao = db.listaDao()
                    val repository = ListaRepository(dao)

                    MainListScreen(
                        listId = listId,
                        repository = repository,

                        onAddClick = {
                            navController.navigate("addItem/$listId")
                        },

                        onStatsClick = {
                            navController.navigate("stats/$listId")
                        }
                    )
                }

                // 🟢 Pantalla principal de lista
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
    private suspend fun cargarUsuariosDesdeJson() {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.listaDao()

        //if (dao.getUsuarios().isNotEmpty()) return
        dao.deleteAllUsuarios()

        try {
            // Leer desde res/raw/users.json
            val json = resources.openRawResource(R.raw.users)
                .bufferedReader().use { it.readText() }

            val usuarios: List<Usuario> = Gson().fromJson(
                json,
                object : TypeToken<List<Usuario>>() {}.type
            )

            usuarios.forEach { dao.insertUsuario(it) }
            Log.d("DB", "${usuarios.size} usuarios insertados desde JSON")
        } catch (e: Exception) {
            Log.e("DB", "Error cargando usuarios desde JSON", e)
        }
    }

    private suspend fun cargarListasDesdeJson() {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.listaDao()

        dao.deleteAllListas()
        dao.deleteAllCrossRefs()   // ← IMPORTANTE

        try {
            // Leer desde res/raw/users.json
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
            Log.d("DB LISTAS", "${listas.size} listas insertados desde JSON")
        } catch (e: Exception) {
            Log.e("DB", "Error cargando listas desde JSON", e)
        }
    }

    private suspend fun cargarListaUsuarioDesdeJson() {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.listaDao()

        dao.deleteAllListaUsuarios() // opcional, si tienes este método

        try {
            val json = resources.openRawResource(R.raw.listusers)
                .bufferedReader().use { it.readText() }

            val relaciones: List<ListaUsuarioCrossRef> = Gson().fromJson(
                json,
                object : TypeToken<List<ListaUsuarioCrossRef>>() {}.type
            )

            relaciones.forEach { dao.insertListaUsuarioCrossRef(it) }

            Log.d("DB", "${relaciones.size} relaciones insertadas desde JSON")

        } catch (e: Exception) {
            Log.e("DB", "Error cargando lista_usuario desde JSON", e)
        }
    }

    private suspend fun cargarItemsDesdeJson() {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.listaDao()

        dao.deleteAllItems()

        try {
            val json = resources.openRawResource(R.raw.items)
                .bufferedReader().use { it.readText() }

            val items: List<Item> = Gson().fromJson(
                json,
                object : TypeToken<List<Item>>() {}.type
            )

            items.forEach { dao.insertItem(it) }

            Log.d("DB", "${items.size} items insertados desde JSON")

        } catch (e: Exception) {
            Log.e("DB", "Error cargando items desde JSON", e)
        }
    }



    suspend fun asignarUsuarioALista(dao: ListaDao, listaId: Int, usuarioId: Int) {
        dao.insertCrossRef(
            ListaUsuarioCrossRef(listaId, usuarioId)
        )
    }

    private fun insertarEnGrocersyncDB() {

        val data = hashMapOf(
            "mensaje" to "Firebase funciona",
            "usuario" to usuarioActualId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("GrocersyncDB")
            .add(data)
            .addOnSuccessListener { documentReference ->

                Log.d("FIREBASE", "Insertado con ID: ${documentReference.id}")

            }
            .addOnFailureListener { e ->

                Log.e("FIREBASE", "Error insertando", e)
            }
    }




}