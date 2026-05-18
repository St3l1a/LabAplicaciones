package com.example.grocersync.database

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SyncRepository(
    private val db: FirebaseFirestore,
    private val dao: ListaDao,
) {

    // ---------------------------
    // SUBIR LISTAS A FIREBASE
    // ---------------------------
    suspend fun syncListasToFirebase() {
        val listas = dao.getListas()

        listas.forEach { lista ->
            db.collection("listas")
                .document(lista.id.toString())
                .set(lista)
        }
    }

    // ---------------------------
    // SUBIR ITEMS
    // ---------------------------
    suspend fun syncItemsToFirebase() {
        val items = dao.getAllItems()

        items.forEach { item ->
            db.collection("items")
                .document(item.id.toString())
                .set(item)
        }
    }

    // ---------------------------
    // BAJAR LISTAS DE FIREBASE
    // ---------------------------
    fun syncListasFromFirebase(onComplete: () -> Unit = {}) {
        db.collection("listas")
            .get()
            .addOnSuccessListener { result ->

                result.documents.forEach { doc ->
                    val lista = doc.toObject(Lista::class.java)
                    if (lista != null) {
                        GlobalScope.launch(Dispatchers.IO) {
                            dao.insertLista(lista)
                        }
                    }
                }

                onComplete()
            }
    }

    // ---------------------------
// SUBIR USUARIOS
// ---------------------------
    suspend fun syncUsuariosToFirebase() {

        val usuarios = dao.getUsuarios()

        usuarios.forEach { usuario ->

            db.collection("users")
                .document(usuario.id.toString())
                .set(usuario)
        }
    }

    // ---------------------------
    // SYNC COMPLETO
    // ---------------------------
    fun fullSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncListasToFirebase()
            syncItemsToFirebase()
            syncUsuariosToFirebase()
        }

        syncListasFromFirebase()
    }

}