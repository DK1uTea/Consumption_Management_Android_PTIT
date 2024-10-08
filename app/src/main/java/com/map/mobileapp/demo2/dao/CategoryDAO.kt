package com.map.mobileapp.demo2.dao

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.map.mobileapp.demo2.model.Category

class CategoryDAO(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // Tìm kiếm các danh mục dựa trên loại InOut (income = true, expense = false)
    fun searchByInOut(isIncome: Boolean): List<Category> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val categories = mutableListOf<Category>()

        // Assuming id=1 for income and id=2 for expense
        val inOutId = if (isIncome) 1 else 2

        val cursor: Cursor = db.rawQuery(
            """
        SELECT tblCategory.id, tblCategory.name, tblCategory.idParent, tblCategory.icon, tblCategory.note 
        FROM tblCategory
        JOIN tblCatInOut ON tblCategory.id = tblCatInOut.idCat
        WHERE tblCatInOut.idInOut = ?
        """, arrayOf(inOutId.toString())
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val idParent = cursor.getInt(cursor.getColumnIndexOrThrow("idParent"))
            val icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))

            // Create a Category object
            val category = Category(id, name, idParent, icon, note)
            categories.add(category)
        }

        cursor.close()
        db.close()

        return categories
    }

}
