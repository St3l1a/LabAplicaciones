package com.example.grocersync.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class ListaRepository(
    private val dao: ListaDao,
    private val db: FirebaseFirestore,
    private val context: Context
) {

    // =====================================================
    // INTERNET
    // =====================================================

    private fun hayInternet(): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // =====================================================
    // LISTAS
    // =====================================================

    suspend fun getListas() = dao.getListas()

    suspend fun insertLista(lista: Lista) {

        // LOCAL
        dao.insertLista(lista)

        // FIREBASE
        if (hayInternet()) {

            db.collection("listas")
                .document(lista.id.toString())
                .set(lista)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "Lista subida")
                }
                .addOnFailureListener {
                    Log.e("FIREBASE", "Error lista")
                }
        }
    }

    suspend fun deleteLista(lista: Lista) {

        dao.deleteLista(lista)

        if (hayInternet()) {

            db.collection("listas")
                .document(lista.id.toString())
                .delete()
        }
    }

    // =====================================================
    // ITEMS
    // =====================================================

    suspend fun getItems(listaId: Int) = dao.getItems(listaId)

    suspend fun insertItem(item: Item) {

        // SIEMPRE ROOM
        dao.insertItem(item)

        // SI HAY INTERNET → FIREBASE
        if (hayInternet()) {

            db.collection("items")
                .document(item.id.toString())
                .set(item)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "Item subido")
                }
                .addOnFailureListener {
                    Log.e("FIREBASE", "Error item")
                }
        }
    }

    suspend fun updateItem(item: Item) {

        dao.updateItem(item)

        if (hayInternet()) {

            db.collection("items")
                .document(item.id.toString())
                .set(item)
        }
    }

    suspend fun deleteItem(item: Item) {

        dao.deleteItem(item)

        if (hayInternet()) {

            db.collection("items")
                .document(item.id.toString())
                .delete()
        }
    }

    // =====================================================
    // SYNC
    // =====================================================

    suspend fun syncPendingData() {

        if (!hayInternet()) return

        // LISTAS
        dao.getListas().forEach { lista ->

            db.collection("listas")
                .document(lista.id.toString())
                .set(lista)
        }

        // ITEMS
        dao.getAllItems().forEach { item ->

            db.collection("items")
                .document(item.id.toString())
                .set(item)
        }

        // USUARIOS
        dao.getUsuarios().forEach { usuario ->

            db.collection("users")
                .document(usuario.id.toString())
                .set(usuario)
        }

        Log.d("FIREBASE", "SYNC COMPLETO")
    }

    // =====================================================
    // RELACIONES
    // =====================================================

    suspend fun getListasConItems() = dao.getListasConItems()

    // =====================================================
    // LOGIN
    // =====================================================

    suspend fun login(email: String, password: String): Usuario? {
        return dao.login(email, password)
    }

    // =====================================================
    // USUARIO -> LISTAS
    // =====================================================

    suspend fun obtenerListasDeUsuario(
        dao: ListaDao,
        usuarioId: Int
    ): List<Lista> {

        val usuarioConListas =
            dao.getUsuarioConListas(usuarioId)

        return usuarioConListas.listas
    }

    // =====================================================
    // LISTA -> MIEMBROS
    // =====================================================

    suspend fun obtenerMiembrosDeLista(
        dao: ListaDao,
        id: Int
    ): List<String> {

        val listaConUsuarios =
            dao.getListaConUsuarios(id)

        return listaConUsuarios?.usuarios
            ?.map { it.nombre }
            ?: emptyList()
    }

    // =====================================================
    // FLOW
    // =====================================================

    fun getListaConItems(listaId: Int): Flow<ListaConItems> {
        return dao.getListaConItems(listaId)
    }
}