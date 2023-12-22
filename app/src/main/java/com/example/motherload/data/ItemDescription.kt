package com.example.motherload.data
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "itemsdescription")
data class ItemDescription(
    @PrimaryKey val id: String,
    val nom: String,
    val type: String,
    val rarity: String,
    val image: String,
    val desc_fr: String,
    val desc_en: String,
    val quantity: String
)