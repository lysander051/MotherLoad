package com.example.motherload.data
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * @property nom le nom de l'objet
 * @property type le type de l'objet
 * @property rarity la rareté de l'objet
 * @property image le lien vers l'image correspondante à l'objet
 * @property desc_fr la description française de l'objet
 * @property desc_en la description anglaise de l'objet
 * @property quantity la quantité de l'objet possédée par le joueur
 */
@Entity(tableName = "itemsdescription")
data class ItemDescription(
    @PrimaryKey val id: String,
    val nom: String,
    val type: String,
    val rarity: String,
    val image: String,
    val desc_fr: String,
    val desc_en: String,
    var quantity: String
)