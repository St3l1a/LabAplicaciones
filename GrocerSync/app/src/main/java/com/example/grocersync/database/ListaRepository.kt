package com.example.grocersync.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
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
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ========== LISTAS ==========
    suspend fun getListas() = dao.getListas()

    suspend fun insertLista(lista: Lista, usuarioCreadorId: Int) {
        dao.insertLista(lista)
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

    // Método original sin imagen (solo Room + Firestore)
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

    // ========== NUEVO: SUBIDA DE IMAGEN A FIREBASE STORAGE ==========
    private suspend fun uploadImageToFirebase(uri: Uri, itemId: Int): String? {
        return suspendCancellableCoroutine { continuation ->
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("items/${itemId}_${System.currentTimeMillis()}.jpg")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    continuation.resume(downloadUri.toString())
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
            }.addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
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
        // No subas a Firestore la imagen, solo el item (sin ruta local en la nube)
        if (hayInternet()) {
            // Sube el item sin el campo localImagePath (o lo omites en Firestore)
            val itemForFirestore = finalItem.copy(localImagePath = null)
            db.collection("items").document(finalItem.id.toString()).set(itemForFirestore)
        }
        return finalItem
    }

    // Método que inserta el item, sube la imagen y actualiza la URL
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

    suspend fun updateItem(item: Item) {
        dao.updateItem(item)
        if (hayInternet()) {
            db.collection("items")
                .document(item.id.toString())
                .set(item)
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