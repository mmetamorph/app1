package com.example.avtorization

interface UpdateAndDelete {
    fun modifyItem(itemUID : String, isDone : Boolean)
    fun onItemDelete(itemUID: String)
}