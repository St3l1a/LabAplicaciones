package Alexandre.Estrella.uv.es

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteCamping(

    @PrimaryKey
    val campingId: String,

    val nombre: String,
    val municipio: String,
    val provincia: String,
    val categoria: String

)