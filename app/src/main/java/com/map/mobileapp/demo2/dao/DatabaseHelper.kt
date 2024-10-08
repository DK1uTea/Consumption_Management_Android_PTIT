package com.map.mobileapp.demo2.dao

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "expense_manager.db"
        private const val DATABASE_VERSION = 4
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables
        db.execSQL(
            """
            CREATE TABLE tblInOut (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            );
            """
        )

        db.execSQL(
            """
            CREATE TABLE tblCategory (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                idParent INTEGER,
                icon TEXT,
                note TEXT,
                FOREIGN KEY (idParent) REFERENCES tblCategory(id)
            );
            """
        )

        db.execSQL(
            """
            CREATE TABLE tblCatInOut (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                idCat INTEGER NOT NULL,
                idInOut INTEGER NOT NULL,
                FOREIGN KEY (idCat) REFERENCES tblCategory(id),
                FOREIGN KEY (idInOut) REFERENCES tblInOut(id)
            );
            """
        )

        db.execSQL(
            """
            CREATE TABLE tblTransaction (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                idCateInOut INTEGER NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                note TEXT,
                FOREIGN KEY (idCateInOut) REFERENCES tblCatInOut(id)
            );
            """
        )

        // Insert data into tblInOut
        db.execSQL("INSERT INTO tblInOut (name) VALUES ('Income')")
        db.execSQL("INSERT INTO tblInOut (name) VALUES ('Expense')")

        // Get IDs of Income and Expense
        val cursorInOut = db.rawQuery("SELECT id FROM tblInOut WHERE name = 'Income'", null)
        cursorInOut.moveToFirst()
        val inOutIncomeId = cursorInOut.getInt(0)
        cursorInOut.close()

        val cursorInOutExpense = db.rawQuery("SELECT id FROM tblInOut WHERE name = 'Expense'", null)
        cursorInOutExpense.moveToFirst()
        val inOutExpenseId = cursorInOutExpense.getInt(0)
        cursorInOutExpense.close()

        // Insert categories for Income
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Salary', NULL, 'salary_icon', 'Monthly salary')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Freelance', NULL, 'freelance_icon', 'Freelance work')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Scholarship', NULL, 'scholarship_icon', 'Scholarship income')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Money from Parents', NULL, 'parents_icon', 'Money from parents')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Gifts Income', NULL, 'gift_icon', 'Gift income')")

        // Insert categories for Expense
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Tuition', NULL, 'tuition_icon', 'Tuition fees')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Monthly Bill', NULL, 'monthly_bill_icon', 'Monthly Bill Expenses')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Gifts Expense', NULL, 'gift_icon', 'Gift expenses')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Entertainment', NULL, 'entertainment_icon', 'Entertainment expenses')")

        // Get ID of Monthly Bill
        val cursorMonthlyBill = db.rawQuery("SELECT id FROM tblCategory WHERE name = 'Monthly Bill'", null)
        cursorMonthlyBill.moveToFirst()
        val monthlyBillId = cursorMonthlyBill.getInt(0)
        cursorMonthlyBill.close()

        // Insert subcategories for Monthly Bill
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Rent', $monthlyBillId, 'rent_icon', 'Rent payment')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Electricity Bill', $monthlyBillId, 'electricity_icon', 'Electricity expenses')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Water Bill', $monthlyBillId, 'water_icon', 'Water expenses')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Phone Bill', $monthlyBillId, 'phone_icon', 'Phone expenses')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Food', $monthlyBillId, 'food_icon', 'Food and groceries')")
        db.execSQL("INSERT INTO tblCategory (name, idParent, icon, note) VALUES ('Groceries', $monthlyBillId, 'groceries_icon', 'Market expenses')")

        // Insert into tblCatInOut
        val cursorCategory = db.rawQuery("SELECT id, name FROM tblCategory", null)
        while (cursorCategory.moveToNext()) {
            val categoryId = cursorCategory.getInt(cursorCategory.getColumnIndexOrThrow("id"))
            val categoryName = cursorCategory.getString(cursorCategory.getColumnIndexOrThrow("name"))
            if (categoryName in listOf("Salary", "Freelance", "Scholarship", "Money from Parents", "Gifts Income")) {
                db.execSQL("INSERT INTO tblCatInOut (idCat, idInOut) VALUES ($categoryId, $inOutIncomeId)")
            } else {
                db.execSQL("INSERT INTO tblCatInOut (idCat, idInOut) VALUES ($categoryId, $inOutExpenseId)")
            }
        }
        cursorCategory.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop tables if they exist and recreate them
        db.execSQL("DROP TABLE IF EXISTS tblTransaction")
        db.execSQL("DROP TABLE IF EXISTS tblCatInOut")
        db.execSQL("DROP TABLE IF EXISTS tblCategory")
        db.execSQL("DROP TABLE IF EXISTS tblInOut")
        onCreate(db)
    }


}
