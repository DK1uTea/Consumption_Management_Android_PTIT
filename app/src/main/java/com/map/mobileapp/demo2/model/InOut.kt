package com.map.mobileapp.demo2.model

import java.io.Serializable

class InOut(
    private var id: Int,
    private var name: String
) : Serializable {

    fun getId(): Int {
        return id
    }

    fun getName(): String {
        return name
    }

}
