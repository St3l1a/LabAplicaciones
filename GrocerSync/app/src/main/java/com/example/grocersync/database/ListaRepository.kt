package com.example.grocersync.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private var relationsListener: ListenerRegistration? = null  // solo una declaración

    /**
     * Inicia la escucha en Firestore para la colección "listaUsuarios"
     * Cada cambio se guarda en Room, y los Flows del DAO notificarán a la UI.
     */
    fun startListeningToRelations() {
        relationsListener?.remove()
        relationsListener = db.collection("listaUsuarios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Error en listener", error)
                    return@addSnapshotListener
                }
                snapshot?.documentChanges?.forEach { change ->
                    val crossRef = change.document.toObject(ListaUsuarioCrossRef::class.java)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                dao.insertListaUsuarioCrossRef(crossRef)
                                Log.d("SYNC", "Relación añadida/modificada: ${crossRef.listaId} - ${crossRef.usuarioId}")
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                dao.deleteListaUsuarioCrossRef(crossRef.listaId, crossRef.usuarioId)
                                Log.d("SYNC", "Relación eliminada")
                            }
                        }
                    }
                }
            }
    }

    fun stopListeningToRelations() {
        relationsListener?.remove()
    }

    // Flows expuestos a la UI
    fun obtenerListasDeUsuario(usuarioId: Int): Flow<List<Lista>> =
        dao.getListasDelUsuario(usuarioId)

    fun obtenerMiembrosDeLista(listaId: Int): Flow<List<String>> =
        dao.getMiembrosDeLista(listaId)

    // ========== RESTANTE DEL REPOSITORIO (igual que antes, sin duplicados) ==========
    private fun hayInternet(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getListas() = dao.getListas()

    suspend fun insertLista(lista: Lista, usuarioCreadorId: Int): Long {
        // 1. Insertar la lista
        val id = dao.insertLista(lista)
        // 2. Relación creador (necesaria para que aparezca como miembro)
        val crossRefCreador = ListaUsuarioCrossRef(lista.id, usuarioCreadorId)
        dao.insertListaUsuarioCrossRef(crossRefCreador)
        // 3. Si hay internet, subir lista y relación a Firestore
        if (hayInternet()) {
            db.collection("listas").document(lista.id.toString()).set(lista)
            db.collection("listaUsuarios")
                .document("${lista.id}_$usuarioCreadorId")
                .set(crossRefCreador)
                .addOnSuccessListener { Log.d("FIREBASE", "Relación creador subida") }
        }
        return id
    }

    suspend fun deleteLista(lista: Lista) {
        dao.deleteLista(lista)
        if (hayInternet()) {
            db.collection("listas").document(lista.id.toString()).delete()
        }
    }

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
        item.localImagePath?.let { path ->
            try { File(path).delete() } catch (_: Exception) {}
        }
        dao.deleteItem(item)
        if (hayInternet()) {
            db.collection("items").document(item.id.toString()).delete()
        }
    }

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
                    CoroutineScope(Dispatchers.IO).launch {
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

    suspend fun getListasConItems() = dao.getListasConItems()
    suspend fun login(email: String, password: String): Usuario? = dao.login(email, password)
    suspend fun obtenerListasDeUsuario(dao: ListaDao, usuarioId: Int): List<Lista> {
        return dao.getUsuarioConListas(usuarioId).listas
    }
    suspend fun obtenerMiembrosDeLista(dao: ListaDao, id: Int): List<String> {
        return dao.getListaConUsuarios(id)?.usuarios?.map { it.nombre } ?: emptyList()
    }
    fun getListaConItems(listaId: Int): Flow<ListaConItems> = dao.getListaConItems(listaId)

    suspend fun addMemberToList(listaId: Int, userEmail: String): Boolean {
        val usuario = dao.getUsuarioByEmail(userEmail) ?: return false
        val existing = dao.getListaConUsuarios(listaId)?.usuarios?.any { it.id == usuario.id } == true
        if (existing) return false

        val crossRef = ListaUsuarioCrossRef(listaId, usuario.id)
        dao.insertListaUsuarioCrossRef(crossRef)

        if (hayInternet()) {
            db.collection("listaUsuarios")
                .document("${listaId}_${usuario.id}")
                .set(crossRef)
                .addOnSuccessListener { Log.d("SHARE", "Miembro añadido a Firestore") }
                .addOnFailureListener { Log.e("SHARE", "Error al subir relación", it) }
        }
        return true
    }

    suspend fun syncRelationsFromFirestore() {
        if (!hayInternet()) return
        suspendCancellableCoroutine<Unit> { continuation ->
            db.collection("listaUsuarios").get()
                .addOnSuccessListener { snapshot ->
                    val relaciones = mutableListOf<ListaUsuarioCrossRef>()
                    for (document in snapshot.documents) {
                        val crossRef = document.toObject(ListaUsuarioCrossRef::class.java)
                        if (crossRef != null) {
                            relaciones.add(crossRef)
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        relaciones.forEach { crossRef ->
                            dao.insertListaUsuarioCrossRef(crossRef)
                        }
                        Log.d("SYNC", "Relaciones sincronizadas: ${relaciones.size}")
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SYNC", "Error sync relaciones", e)
                    continuation.resumeWithException(e)
                }
        }
    }


}