package com.example.grocersync.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.grocersync.database.ShoppingList

@Dao
interface ShoppingDao {

    // LISTAS
    @Query("SELECT * FROM shopping_lists")
    suspend fun getAllLists(): List<ShoppingList>

    @Insert
    suspend fun insertList(list: ShoppingList)

    @Delete
    suspend fun deleteList(list: ShoppingList)

    // ITEMS
    @Query("SELECT * FROM items WHERE listId = :listId")
    suspend fun getItemsFromList(listId: Int): List<Item>

    @Insert
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)
}