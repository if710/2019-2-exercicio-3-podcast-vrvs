package br.ufpe.cin.android.podcast

import androidx.room.*

@Entity(tableName = "ItemFeed")
data class ItemFeed(@PrimaryKey val title: String, val link: String, val pubDate: String, val description: String, val downloadLink: String, var path: String, var duration : Int) {

    override fun toString(): String {
        return title
    }
}

// Statements necessarios para fazer as questoes
@Dao
interface ItemFeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: ItemFeed)

    @Query("SELECT * FROM ItemFeed")
    fun findAll() : List<ItemFeed>

    @Query("UPDATE ItemFeed SET path = :path WHERE title LIKE :title")
    fun addPath(title : String, path: String)

    @Query("SELECT * FROM ItemFeed WHERE title LIKE :title")
    fun search(title : String) : ItemFeed

    @Query("UPDATE ItemFeed SET duration = :duration WHERE title LIKE :title")
    fun addDuration(title : String, duration: Int)
}