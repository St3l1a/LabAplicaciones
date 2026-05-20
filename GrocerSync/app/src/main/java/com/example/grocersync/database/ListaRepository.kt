package com.example.grocersync.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ListaRepository(
    public val dao: ListaDao,
    private val db: FirebaseFirestore,
    private val context: Context
) {

    private fun hayInternet(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ========== LISTAS ==========
    suspend fun getListas() = dao.getListas()

    suspend fun insertLista(lista: Lista, usuarioCreadorId: Int): Long {
        val id = dao.insertLista(lista)
        val crossRef = ListaUsuarioCrossRef(lista.id, usuarioCreadorId)
        dao.insertListaUsuarioCrossRef(crossRef)
        if (hayInternet()) {
            db.collection("listas").document(lista.id.toString()).set(lista)
                .addOnSuccessListener { Log.d("FIREBASE", "Lista subida") }
                .addOnFailureListener { Log.e("FIREBASE", "Error lista") }
        }
        return id
    }

    suspend fun deleteLista(lista: Lista) {
        dao.deleteLista(lista)
        if (hayInternet()) {
            db.collection("listas").document(lista.id.toString()).delete()
        }
    }

    // ========== ITEMS ==========
    suspend fun getItems(listaId: Int) = dao.getItems(listaId)
    fun getItemsFlow(listaId: Int): Flow<List<Item>> = dao.getItemsFlow(listaId)

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val fileName = "item_${System.currentTimeMillis()}.jpg"
            val destFile = File(imagesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e("STORAGE", "Error guardando imagen local", e)
            null
        }
    }

    suspend fun insertItemWithImage(item: Item, imageUri: Uri?): Item {
        val itemId = dao.insertItem(item)
        var localPath: String? = null
        if (imageUri != null) {
            localPath = saveImageToInternalStorage(imageUri)
        }
        val finalItem = item.copy(id = itemId.toInt(), localImagePath = localPath)
        dao.updateItem(finalItem)
        if (hayInternet()) {
            val itemForFirestore = finalItem.copy(localImagePath = null)
            db.collection("items").document(finalItem.id.toString()).set(itemForFirestore)
                .addOnSuccessListener { Log.d("FIREBASE", "Item subido a Firestore") }
                .addOnFailureListener { Log.e("FIREBASE", "Error subiendo item") }
        }
        return finalItem
    }

    suspend fun updateItem(item: Item) {
        dao.updateItem(item)
        if (hayInternet()) {
            val itemForFirestore = item.copy(localImagePath = null)
            db.collection("items").document(item.id.toString()).set(itemForFirestore)
        }
    }

    suspend fun deleteItem(item: Item) {
        // Borrar imagen local si existe
        item.localImagePath?.let { path ->
            try { File(path).delete() } catch (_: Exception) {}
        }
        dao.deleteItem(item)
        if (hayInternet()) {
            db.collection("items").document(item.id.toString()).delete()
        }
    }

    // ========== SINCRONIZACIÓN (DESDE FIRESTORE A ROOM) ==========
    suspend fun syncItemsFromFirestore(listaId: Int) {
        if (!hayInternet()) return

        suspendCancellableCoroutine<Unit> { continuation ->

            db.collection("items")
                .whereEqualTo("listaId", listaId)
                .get()
                .addOnSuccessListener { snapshot ->

                    val itemsFromFirestore = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Item::class.java)
                    }

                    // 🔥 AQUÍ la clave: lanzar corrutina
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        itemsFromFirestore.forEach { item ->
                            dao.insertOrUpdateItem(item.copy(localImagePath = null))
                        }
                        Log.d("SYNC", "Sincronizados ${itemsFromFirestore.size} items")

                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    // ========== SYNC SUBIDA (LOCAL -> FIRESTORE) ==========
    suspend fun syncPendingData() {
        if (!hayInternet()) return
        dao.getListas().forEach { lista ->
            db.collection("listas").document(lista.id.toString()).set(lista)
        }
        dao.getAllItems().forEach { item ->
            db.collection("items").document(item.id.toString()).set(item.copy(localImagePath = null))
        }
        dao.getUsuarios().forEach { usuario ->
            db.collection("users").document(usuario.id.toString()).set(usuario)
        }
        Log.d("FIREBASE", "SYNC COMPLETO")
    }

    // ========== RELACIONES Y FLUJOS ==========
    suspend fun getListasConItems() = dao.getListasConItems()
    suspend fun login(email: String, password: String): Usuario? = dao.login(email, password)
    suspend fun obtenerListasDeUsuario(dao: ListaDao, usuarioId: Int): List<Lista> {
        return dao.getUsuarioConListas(usuarioId).listas
    }
    suspend fun obtenerMiembrosDeLista(dao: ListaDao, id: Int): List<String> {
        return dao.getListaConUsuarios(id)?.usuarios?.map { it.nombre } ?: emptyList()
    }
    fun getListaConItems(listaId: Int): Flow<ListaConItems> = dao.getListaConItems(listaId)
}