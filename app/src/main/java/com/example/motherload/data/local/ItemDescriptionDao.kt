package com.example.motherload.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.motherload.data.ItemDescription

@Dao
interface ItemDescriptionDao {
    @Insert
    suspend fun insertItem(item: ItemDescription)

    @Query("SELECT * FROM itemsdescription WHERE id IN (:itemIds)")
    suspend fun getItemsByIds(itemIds: List<String>): MutableList<ItemDescription>

}