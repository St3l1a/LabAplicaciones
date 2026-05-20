package com.example.grocersync.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListaDao {

    // LISTAS
    @Query("SELECT * FROM listas")
    suspend fun getListas(): List<Lista>

    @Transaction
    @Query("SELECT * FROM listas WHERE id = :listaId")
    suspend fun getListaConUsuarios(listaId: Int): ListaConUsuarios?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLista(lista: Lista): Long

    @Delete
    suspend fun deleteLista(lista: Lista)

    @Query("DELETE FROM listas")
    suspend fun deleteAllListas()

    // ITEMS
    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT * FROM items WHERE listaId = :listaId")
    suspend fun getItems(listaId: Int): List<Item>

    @Query("SELECT * FROM items WHERE listaId = :listaId")
    fun getItemsFlow(listaId: Int): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateItem(item: Item)

    // RELACIÓN LISTA-USUARIO
    @Transaction
    @Query("SELECT * FROM listas")
    suspend fun getListasConItems(): List<ListaConItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: ListaUsuarioCrossRef)

    @Query("DELETE FROM ListaUsuarioCrossRef")
    suspend fun deleteAllCrossRefs()

    // USUARIOS
    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios")
    suspend fun getUsuarios(): List<Usuario>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListaUsuarioCrossRef(crossRef: ListaUsuarioCrossRef)

    @Query("DELETE FROM listausuariocrossref")
    suspend fun deleteAllListaUsuarios()

    @Transaction
    @Query("SELECT * FROM listas")
    suspend fun getListasConUsuarios(): List<ListaConUsuarios>

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?

    @Transaction
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    suspend fun getUsuarioConListas(usuarioId: Int): UsuarioConListas

    @Transaction
    @Query("SELECT * FROM listas WHERE id = :listaId")
    fun getListaConItems(listaId: Int): Flow<ListaConItems>

    @Query("SELECT COUNT(*) FROM ListaUsuarioCrossRef")
    suspend fun getCountListaUsuarioCrossRef(): Int

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): Item?
}