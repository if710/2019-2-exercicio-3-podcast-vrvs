package br.ufpe.cin.android.podcast

import androidx.room.*

@Entity(tableName = "ItemFeed")
data class ItemFeed(@PrimaryKey val title: String, val link: String, val pubDate: String, val description: String, val downloadLink: String) {

    override fun toString(): String {
        return title
    }
}

@Dao
interface ItemFeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: ItemFeed)

    @Query("SELECT * FROM ItemFeed")
    fun findAll() : List<ItemFeed>
}