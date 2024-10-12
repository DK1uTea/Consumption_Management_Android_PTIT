package com.map.mobileapp.demo2.model

data class Category(
    private val id: Int? = null,
    private val name: String,
    private val idParent: Int?,
    private val icon: String?,
    private val note: String?
) {
    // Getters for Category fields
    fun getId(): Int? = id
    fun getName(): String = name // Access name of the Category
    fun getIdParent(): Int? = idParent
    fun getIcon(): String? = icon
    fun getNote(): String? = note
}

