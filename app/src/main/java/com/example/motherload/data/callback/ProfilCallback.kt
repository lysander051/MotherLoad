package com.example.motherload.data.callback

import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription

interface ProfilCallback {
    fun changerPseudo(pseudo: String)
    fun resetUser()
    fun getArtifact(inventory: List<Item>)
}