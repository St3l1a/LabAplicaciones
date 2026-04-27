package com.example.grocersync.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Lista::class,
        Item::class,
        Usuario::class,
        ListaUsuarioCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listaDao(): ListaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grocersync_db"
                )
                    .fallbackToDestructiveMigration() // ⚠️ borra datos si cambias versión
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}