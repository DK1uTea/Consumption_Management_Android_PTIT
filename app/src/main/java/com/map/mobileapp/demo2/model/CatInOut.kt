package com.map.mobileapp.demo2.model

data class CatInOut(
    private val id: Int?,
    private val category: Category,  // Reference to Category
    private val inOut: InOut
) {
    // Getters for CatInOut fields
    fun getId(): Int? = id
    fun getCategory(): Category = category // Access Category from CatInOut
    fun getInOut(): InOut = inOut
}

