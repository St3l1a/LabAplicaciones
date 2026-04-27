package com.example.grocersync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.grocersync.database.AppDatabase
import com.example.grocersync.database.Usuario
import com.example.grocersync.screen.AddItemScreen
import com.example.grocersync.screen.LoginScreen
import com.example.grocersync.screen.StatisticsScreen
import com.example.grocersync.ui.MainListScreen
import com.example.grocersync.ui.SelectListScreen
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
// 📥 Cargar usuarios desde JSON si la BD está vacía
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                cargarUsuariosDesdeJson()
            }
        }
        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "login" // 👈 AQUÍ estaba el error
            ) {

                // 🔵 LOGIN (primera pantalla)
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("select") {
                                popUpTo("login") { inclusive = true } // evita volver atrás al login
                            }
                        }
                    )
                }

                // 🟡 Selección de lista
                composable("select") {
                    SelectListScreen(
                        onBack = { /* opcional */ },
                        onListSelected = { listName ->
                            navController.navigate("main/$listName")
                        }
                    )
                }

                // 🟢 Pantalla principal de lista
                composable("main/{listName}") { backStackEntry ->
                    val listName = backStackEntry.arguments?.getString("listName") ?: ""

                    MainListScreen(
                        listName = listName,
                        onAddClick = {
                            navController.navigate("add_item")
                        },
                        onStatsClick = {
                            navController.navigate("stats") // o lo que quieras hacer
                        }
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

        if (dao.getUsuarios().isNotEmpty()) return

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
}