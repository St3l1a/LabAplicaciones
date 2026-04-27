package com.example.grocersync.database

class ListaRepository(private val dao: ListaDao) {

    suspend fun getListas() = dao.getListas()

    suspend fun insertLista(lista: Lista) = dao.insertLista(lista)

    suspend fun deleteLista(lista: Lista) = dao.deleteLista(lista)

    suspend fun getItems(listaId: Int) = dao.getItems(listaId)

    suspend fun insertItem(item: Item) = dao.insertItem(item)

    suspend fun updateItem(item: Item) = dao.updateItem(item)

    suspend fun deleteItem(item: Item) = dao.deleteItem(item)

    suspend fun getListasConItems() = dao.getListasConItems()
}