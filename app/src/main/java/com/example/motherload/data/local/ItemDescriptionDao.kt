package com.example.motherload.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motherload.data.ItemDescription

@Dao
interface ItemDescriptionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItem(item: ItemDescription)
    @Query("SELECT * FROM itemsdescription WHERE id IN (:itemIds)")
    fun getItemsByIds(itemIds: List<String>): MutableList<ItemDescription>
    @Query("DELETE FROM itemsdescription")
    fun deleteAll()
}