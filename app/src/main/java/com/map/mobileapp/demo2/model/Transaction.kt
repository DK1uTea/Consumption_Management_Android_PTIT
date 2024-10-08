package com.map.mobileapp.demo2.model

import java.io.Serializable
import java.util.Date

data class Transaction(
    private val id: Int?,
    private val name: String,
    private val catInOut: CatInOut, // Reference to CatInOut
    private val amount: Double,
    private val date: Date,
    private val note: String?
) {
    // Getters for Transaction fields
    fun getId(): Int? = id
    fun getName(): String = name
    fun getCatInOut(): CatInOut = catInOut // Access CatInOut from Transaction
    fun getAmount(): Double = amount
    fun getDate(): Date = date
    fun getNote(): String? = note
}

