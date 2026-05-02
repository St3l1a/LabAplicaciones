package com.example.grocersync.database


import androidx.room.*

@Dao
interface ListaDao {

    // 🔹 LISTAS
    @Query("SELECT * FROM listas")
    suspend fun getListas(): List<Lista>

    @Insert suspend fun insertLista(lista: Lista)


    @Delete
    suspend fun deleteLista(lista: Lista)

    @Query("DELETE FROM listas")
    suspend fun deleteAllListas()

    // 🔹 ITEMS
    @Query("SELECT * FROM items WHERE listaId = :listaId")
    suspend fun getItems(listaId: Int): List<Item>

    @Insert
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    // 🔹 RELACIÓN
    @Transaction
    @Query("SELECT * FROM listas")
    suspend fun getListasConItems(): List<ListaConItems>


    @Insert suspend fun insertCrossRef(crossRef: ListaUsuarioCrossRef)

    // 🔹 USUARIOS


    // Borrar todos los usuarios
    @Query("DELETE FROM usuarios")
    suspend fun deleteAllUsuarios()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

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

    @Transaction
    @Query("SELECT * FROM usuarios WHERE id = :usuarioId")
    suspend fun getUsuarioConListas(usuarioId: Int): UsuarioConListas

}

