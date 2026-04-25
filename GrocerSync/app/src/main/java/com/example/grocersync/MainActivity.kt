package com.example.grocersync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.grocersync.screen.AddItemScreen
import com.example.grocersync.screen.LoginScreen
import com.example.grocersync.ui.MainListScreen
import com.example.grocersync.ui.SelectListScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        }
                    )
                }

                composable("add_item") {
                    AddItemScreen()
                }


            }
        }
    }
}