package com.example.motherload.Data

import android.media.Image

data class ItemDescription(
    val id : String,
    val nom: String,
    val type: String,
    val rarity: String,
    val image: String,
    val desc_fr: String,
    val desc_en: String,
    val quantity : String)