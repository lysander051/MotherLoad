package com.example.motherload.Data

interface InventoryCallback {
    fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>)
}