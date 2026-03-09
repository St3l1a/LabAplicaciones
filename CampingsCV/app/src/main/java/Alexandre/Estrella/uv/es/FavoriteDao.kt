package Alexandre.Estrella.uv.es

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    suspend fun getAll(): List<FavoriteCamping>

    @Insert
    suspend fun insert(favorite: FavoriteCamping)

    @Delete
    suspend fun delete(favorite: FavoriteCamping)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE campingId = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT * FROM favorites WHERE campingId = :id LIMIT 1")
    fun getById(id: String): FavoriteCamping?

    @Query("DELETE FROM favorites WHERE campingId = :id")
    fun deleteById(id: String)
}