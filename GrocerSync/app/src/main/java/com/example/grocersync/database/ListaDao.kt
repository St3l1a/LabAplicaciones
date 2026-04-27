package com.example.grocersync.database


import androidx.room.*

@Dao
interface ListaDao {

    // 🔹 LISTAS
    @Query("SELECT * FROM listas")
    suspend fun getListas(): List<Lista>

    @Insert
    suspend fun insertLista(lista: Lista): Long

    @Delete
    suspend fun deleteLista(lista: Lista)

    // 🔹 ITEMS
    @Query("SELECT * FROM items WHERE listaId = :listaId")
    suspend fun getItems(listaId: Int): List<Item>

    @Insert
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    // 🔹 RELACIÓN
    @Transaction
    @Query("SELECT * FROM listas")
    suspend fun getListasConItems(): List<ListaConItems>

    // 🔹 USUARIOS
    @Insert
    suspend fun insertUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios")
    suspend fun getUsuarios(): List<Usuario>

    // 🔹 RELACIONAR USUARIO CON LISTA
    @Insert
    suspend fun insertListaUsuarioCrossRef(crossRef: ListaUsuarioCrossRef)

    // 🔹 OBTENER LISTAS CON USUARIOS
    @Transaction
    @Query("SELECT * FROM listas")
    suspend fun getListasConUsuarios(): List<ListaConUsuarios>
    // 🔐 LOGIN
    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?
}

