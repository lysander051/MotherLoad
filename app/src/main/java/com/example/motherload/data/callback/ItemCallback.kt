package com.example.motherload.data.callback

import com.example.motherload.data.ItemDescription

interface ItemCallback {
    fun getItemsDescription(itemDescription: MutableList<ItemDescription>)
}