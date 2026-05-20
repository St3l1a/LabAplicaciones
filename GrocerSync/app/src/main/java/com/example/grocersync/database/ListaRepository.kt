package com.example.grocersync.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class ListaRepository(
    public val dao: ListaDao,
    private val db: FirebaseFirestore,
    private val context: Context
) {

    private fun hayInternet(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ========== LISTAS ==========
    suspend fun getListas() = dao.getListas()

    // ✅ Cambiamos: ahora recibe el usuario creador
    suspend fun insertLista(lista: Lista, usuarioCreadorId: Int) {
        dao.insertLista(lista)
        // Guardamos la relación con el creador
        val crossRef = ListaUsuarioCrossRef(lista.id, usuarioCreadorId)
        dao.insertListaUsuarioCrossRef(crossRef)

        if (hayInternet()) {
            db.collection("listas")
                .document(lista.id.toString())
                .set(lista)
                .addOnSuccessListener { Log.d("FIREBASE", "Lista subida") }
                .addOnFailureListener { Log.e("FIREBASE", "Error lista") }
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

    // ========== ITEMS ==========
    suspend fun getItems(listaId: Int) = dao.getItems(listaId)

    suspend fun insertItem(item: Item) {
        dao.insertItem(item)
        if (hayInternet()) {
            db.collection("items")
                .document(item.id.toString())
                .set(item)
                .addOnSuccessListener { Log.d("FIREBASE", "Item subido") }
                .addOnFailureListener { Log.e("FIREBASE", "Error item") }
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

    // ========== SYNC ==========
    suspend fun syncPendingData() {
        if (!hayInternet()) return
        dao.getListas().forEach { lista ->
            db.collection("listas").document(lista.id.toString()).set(lista)
        }
        dao.getAllItems().forEach { item ->
            db.collection("items").document(item.id.toString()).set(item)
        }
        dao.getUsuarios().forEach { usuario ->
            db.collection("users").document(usuario.id.toString()).set(usuario)
        }
        Log.d("FIREBASE", "SYNC COMPLETO")
    }

    // ========== RELACIONES ==========
    suspend fun getListasConItems() = dao.getListasConItems()

    suspend fun login(email: String, password: String): Usuario? {
        return dao.login(email, password)
    }

    suspend fun obtenerListasDeUsuario(dao: ListaDao, usuarioId: Int): List<Lista> {
        val usuarioConListas = dao.getUsuarioConListas(usuarioId)
        return usuarioConListas.listas
    }

    suspend fun obtenerMiembrosDeLista(dao: ListaDao, id: Int): List<String> {
        val listaConUsuarios = dao.getListaConUsuarios(id)
        return listaConUsuarios?.usuarios?.map { it.nombre } ?: emptyList()
    }

    fun getListaConItems(listaId: Int): Flow<ListaConItems> {
        return dao.getListaConItems(listaId)
    }
}