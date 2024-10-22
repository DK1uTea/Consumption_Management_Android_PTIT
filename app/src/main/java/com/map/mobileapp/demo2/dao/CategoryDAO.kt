package com.map.mobileapp.demo2.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.PowerManager
import android.util.Log
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
        Log.d("Categories by in out: ", categories.toString())
        return categories
    }

    // Get all categories
    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM tblCategory", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val idParent = cursor.getInt(cursor.getColumnIndexOrThrow("idParent"))
            val icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))

            val category = Category(id, name, idParent, icon, note)
            categories.add(category)
        }

        cursor.close()
        db.close()
        return categories
    }

    // Add a new category to the database and return the new category ID
    fun addCategoryAndGetId(c: Category): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", c.getName())
            put("idParent", c.getIdParent())
            put("icon", c.getIcon())
            put("note", c.getNote())
        }
        val result = db.insert("tblCategory", null, values)
        db.close()
        return result
    }

    // Link the new category with InOut type in tblCatInOut
    fun addCatInOut(categoryId: Int, inOutId: Int): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("idCat", categoryId)
            put("idInOut", inOutId)
        }
        val result = db.insert("tblCatInOut", null, values)
        db.close()
        return result != -1L
    }

    // Get a category by its ID
    fun getCategoryById(id: Int): Category? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM tblCategory WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val idParent = cursor.getInt(cursor.getColumnIndexOrThrow("idParent"))
            val icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))

            val category = Category(categoryId, name, idParent, icon, note)
            cursor.close()
            category
        } else {
            cursor.close()
            null
        }
    }

    // Get child categories by parent ID
    fun getChildCategories(parentId: Int): List<Category> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val categories = mutableListOf<Category>()

        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM tblCategory WHERE idParent = ?", arrayOf(parentId.toString())
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val idParent = cursor.getInt(cursor.getColumnIndexOrThrow("idParent"))
            val icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))

            val category = Category(id, name, idParent, icon, note)
            categories.add(category)
        }

        cursor.close()
        db.close()

        return categories
    }

    // Check if a category has children
    fun hasChildren(categoryId: Int): Boolean {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM tblCategory WHERE idParent = ?", arrayOf(categoryId.toString())
        )

        var hasChildren = false
        if (cursor.moveToFirst()) {
            hasChildren = cursor.getInt(0) > 0
        }

        cursor.close()
        db.close()

        return hasChildren
    }

}
