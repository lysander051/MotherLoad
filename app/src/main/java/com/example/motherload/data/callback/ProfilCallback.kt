package com.example.motherload.data.callback

import com.example.motherload.data.Item

interface ProfilCallback {
    fun changerPseudo(pseudo: String)
    fun resetUser()
    fun getArtifact(artifact: List<Item>)
    fun getInventory(inventory: List<Item>)
}