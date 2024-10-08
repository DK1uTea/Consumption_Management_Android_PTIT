package com.map.mobileapp.demo2.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.map.mobileapp.demo2.model.CatInOut
import com.map.mobileapp.demo2.model.Category
import com.map.mobileapp.demo2.model.InOut
import com.map.mobileapp.demo2.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionDAO(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Add a new transaction to the database
    fun add(t: Transaction): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", t.getName())
            put("idCateInOut", t.getCatInOut().getId())
            put("amount", t.getAmount())
            put("date", dateFormat.format(t.getDate()))
            Log.d("Debug date save to db", "${dateFormat.format(t.getDate())}")
            put("note", t.getNote())
        }
        val result = db.insert("tblTransaction", null, values)
        db.close()
        return result != -1L
    }

    // Edit an existing transaction
    fun edit(t: Transaction): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", t.getName())
            put("idCateInOut", t.getCatInOut().getId())
            put("amount", t.getAmount())
            put("date", dateFormat.format(t.getDate()))
            put("note", t.getNote())
        }
        val result = db.update("tblTransaction", values, "id = ?", arrayOf(t.getId().toString()))
        db.close()
        return result > 0
    }

    // Delete a transaction by its ID
    fun delete(id: Int): Boolean {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val result = db.delete("tblTransaction", "id = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getCategoryById(id: Int): Category? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM tblCategory WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val idParent = cursor.getInt(cursor.getColumnIndexOrThrow("idParent"))
            val icon = cursor.getString(cursor.getColumnIndexOrThrow("icon"))
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))

            val category = Category(
                id = categoryId,
                name = categoryName,
                idParent = idParent,
                icon = icon,
                note = note
            )
            cursor.close()
            category
        } else {
            cursor.close()
            null
        }
    }

    fun getInOutById(id: Int): InOut? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM tblInOut WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val inOutId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val inOutName = cursor.getString(cursor.getColumnIndexOrThrow("name"))

            val inOut = InOut(
                id = inOutId,
                name = inOutName
            )
            cursor.close()
            inOut
        } else {
            cursor.close()
            null
        }
    }

    fun getCatInOutById(id: Int): CatInOut? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM tblCatInOut WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("idCat"))
            val inOutId = cursor.getInt(cursor.getColumnIndexOrThrow("idInOut"))

            val category = getCategoryById(categoryId) // Lấy Category dựa trên id
            val inOut = getInOutById(inOutId) // Lấy InOut dựa trên id

            if (category != null && inOut != null) {
                val catInOut = CatInOut(
                    id = id,
                    category = category,
                    inOut = inOut
                )
                cursor.close()
                catInOut
            } else {
                cursor.close()
                null
            }
        } else {
            cursor.close()
            null
        }
    }

    // get all transactions
    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db: SQLiteDatabase = dbHelper.readableDatabase

        val cursor: Cursor = db.rawQuery("SELECT * FROM tblTransaction", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))

            // Retrieve the date as a String
            val dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"))

            // Parse the date String back to a Date object
            val date = dateFormat.parse(dateString) // Use the same format as when saving
            val note = cursor.getString(cursor.getColumnIndexOrThrow("note"))

            val catInOutId = cursor.getInt(cursor.getColumnIndexOrThrow("idCateInOut"))
            val catInOut = getCatInOutById(catInOutId)

            if (catInOut != null) {
                val transaction = Transaction(
                    id = id,
                    name = name,
                    catInOut = catInOut,
                    amount = amount,
                    date = date,
                    note = note
                )
                Log.d("Debug get transaction date", "${transaction.getDate()}")
                transactions.add(transaction)
            }
        }


        cursor.close()
        db.close()

        return transactions // Ensure to return the list
    }


}
