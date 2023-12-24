package com.example.motherload.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motherload.data.ItemDescription

@Dao
interface ItemDescriptionDao {
    /**
     * Requête SQL pour insérer un item dans la BDD
     *
     * @param item l'item à insérer
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItem(item: ItemDescription)

    /**
     * Requête SQL pour récupérer les items grâce à leur id
     *
     * @param itemIds une liste d'id
     * @return une liste d'ItemDescription
     */
    @Query("SELECT * FROM itemsdescription WHERE id IN (:itemIds)")
    fun getItemsByIds(itemIds: List<String>): MutableList<ItemDescription>

    /**
     * Requête SQL pour reset la BDD
     */
    @Query("DELETE FROM itemsdescription")
    fun deleteAll()
}