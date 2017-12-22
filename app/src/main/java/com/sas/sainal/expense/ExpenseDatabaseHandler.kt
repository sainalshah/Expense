package com.sas.sainal.expense

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


/**
 * Created by sainal on 12/10/17.
 */

class ExpenseDatabaseHandler(context: Context) : SQLiteOpenHelper(context,
        ExpenseDatabaseHandler.DATABASE_NAME, null, ExpenseDatabaseHandler.DATABASE_VERSION) {

    private var dt: Datetime? = null

    enum class Period {
        //Different periods for retrieval of database records.
        TODAY,
        LAST_WEEK, LAST_MONTH, ALL
    }

    enum class Table {
        TYPE, RECORD
    }

    enum class Key {
        //Records table attribute keys
        RECORD_ID,
        RECORD_TYPE, RECORD_AMOUNT, RECORD_DATE,

        //Type table attribute keys
        TYPE_ID,
        TYPE_NAME
    }

    // Kotlin does not allow static variables or functions
    companion object {
        val DATABASE_NAME = "expense_db"
        val DATABASE_VERSION = 1

        val ERROR_NOT_EXIST = -2L
        val ERROR_EXIST = -3L
        val SQL_ERROR: Long = -1

        val SPECIAL_TYPE_INCOME = "income"
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        // Create table with name records, a new column with name id which is INTEGER
        // type and it is primary key for the table, a new column with name
        // type and it is of type TEXT(String), a new column with name amount as
        // TEXT(String) type
        val CREATE_TYPE_TABLE_SCRIPT = "CREATE TABLE " + Table.TYPE.name +
                "(" +
                Key.TYPE_ID.name + " INTEGER PRIMARY KEY," +
                Key.TYPE_NAME.name + " TEXT" +
                ")"
        val CREATE_RECORDS_TABLE_SCRIPT = "CREATE TABLE " + Table.RECORD.name +
                "(" +
                Key.RECORD_ID.name + " INTEGER PRIMARY KEY," +
                Key.RECORD_TYPE.name + " INTEGER," +
                Key.RECORD_AMOUNT.name + " DECIMAL(10, 5)," +
                Key.RECORD_DATE.name + " REAL," +
                "FOREIGN KEY(" + Key.RECORD_TYPE.name + ") REFERENCES " + Table.TYPE.name + " (" + Key.TYPE_ID.name + ")" +
                ")"
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON")
        sqLiteDatabase.execSQL(CREATE_TYPE_TABLE_SCRIPT)
        sqLiteDatabase.execSQL(CREATE_RECORDS_TABLE_SCRIPT)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Drop older record table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ${Table.RECORD.name}")

        // call onCreate method so it creates new table again
        onCreate(sqLiteDatabase)
    }

    /**
     * CRUD Operations (create, Read, Update, Delete)
     */

    private fun typeExists(type: String): Boolean {
        return getTypeId(type) >= 0
    }

    // Adding new SpendRecord
    fun addSpendRecord(record: SpendRecord): Long {
        val typeId = getTypeId(record.type)
        if (typeId >= 0) {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(Key.RECORD_TYPE.name, typeId) // SpendRecord Title
            values.put(Key.RECORD_AMOUNT.name, record.amount) // SpendRecord Content
            values.put(Key.RECORD_DATE.name, record.date)
            // Inserting new row into records table
            return db.insert(Table.RECORD.name, null, values)
        }
        return SQL_ERROR
    }


    // Get Single SpendRecord
    fun getSpendRecord(id: Long): SpendRecord? {
        val db = this.readableDatabase
        var record: SpendRecord? = null

        // SQL query for getting a record from the database
        val selectQuery = """SELECT T1.${Key.RECORD_ID.name}, T2.${Key.TYPE_NAME.name},
                T1.${Key.RECORD_AMOUNT.name},T1.${Key.RECORD_DATE.name} FROM ${Table.RECORD.name} T1, ${Table.TYPE.name} T2
                WHERE T1.${Key.RECORD_TYPE.name}=T2.${Key.TYPE_ID.name} AND T1.${Key.RECORD_ID.name}=$id"""


        val cursor = db.rawQuery(selectQuery, null)


        cursor.let {
            if (cursor.moveToFirst()) {
                record = SpendRecord(
                        cursor.getString(0).toLong(),
                        cursor.getString(1),
                        cursor.getString(2).toDouble(),
                        cursor.getString(3)
                )
            }
        }
        cursor.close()
        // return record
        return record
    }

    // Get All SpendRecords
    private fun getSpendRecords(period: Period, columns: List<String>): Cursor? {

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        var columnStr = ""

        for (column in columns) {
            columnStr += column + ", "
        }
        columnStr = columnStr.substring(0, columnStr.length - 2)

        val incomeID = getTypeId(SPECIAL_TYPE_INCOME)

        val selectQuery: String
        if (period == Period.ALL) {
            // SQL query for getting all records from the database
            selectQuery = """SELECT $columnStr FROM ${Table.RECORD.name} T1, ${Table.TYPE.name} T2
                WHERE T1.${Key.RECORD_TYPE.name}=T2.${Key.TYPE_ID.name} AND T1.${Key.RECORD_TYPE.name}<>$incomeID"""
        } else {

            var periodClause = ""
            when (period) {
                Period.LAST_MONTH -> periodClause = "start of month"
                Period.LAST_WEEK -> periodClause = "-6 days"
                Period.TODAY -> periodClause = "start of day"
            }
            selectQuery = """SELECT $columnStr FROM ${Table.RECORD.name} T1, ${Table.TYPE.name} T2
                WHERE T1.${Key.RECORD_TYPE.name}=T2.${Key.TYPE_ID.name}
                AND T1.${Key.RECORD_DATE.name} BETWEEN datetime('now', '$periodClause') AND datetime('now', 'localtime')
                AND T1.${Key.RECORD_TYPE.name}<>$incomeID"""
        }

        return db.rawQuery(selectQuery, null)

    }

    fun getPeriodSpendingSum(period: Period): Double {

        val cursor = getSpendRecords(period, listOf("SUM(${Key.RECORD_AMOUNT})"))
        cursor.let {
            if (cursor!!.moveToFirst()) {
                return cursor.getDouble(0)
            }
        }
        //return error if the cursor was null
        return SQL_ERROR as Double
    }

    fun getPeriodSpendRecord(period: Period): List<SpendRecord>? {
        val cursor = getSpendRecords(period, listOf(Key.RECORD_ID.name, Key.TYPE_NAME.name,
                Key.RECORD_AMOUNT.name, Key.RECORD_DATE.name))
        cursor.let {
            if (cursor!!.moveToFirst()) {
                val recordList = arrayListOf<SpendRecord>()
                do {
                    val record = SpendRecord(
                            cursor.getString(0).toLong(),
                            cursor.getString(1),
                            cursor.getString(2).toDouble(),
                            cursor.getString(3)
                    )
                    // Adding SpendRecord to list
                    recordList.add(record)
                } while (cursor.moveToNext())

                cursor.close()
                // return recordlist if there is records in database
                return recordList
            }
        }
        //return null if the cursor was null
        return null
    }

    // Updating single record
    fun updateSpendRecord(record: SpendRecord): Long {
        if (record.id != null && typeExists(record.type)) {
            val db = this.writableDatabase
            val updateSql = """UPDATE ${Table.RECORD.name} SET ${Key.RECORD_TYPE}='${getTypeId(record.type)}',
                |${Key.RECORD_AMOUNT}=${record.amount}, ${Key.RECORD_DATE}='${record.date}'
                |WHERE ${Key.RECORD_ID}=${record.id}""".trimMargin()

            db.execSQL(updateSql)
        }
        return SQL_ERROR
    }

    // Deleting single SpendRecord
    fun deleteSpendRecord(id: Long) {
        val db = this.writableDatabase
        db.delete(
                Table.RECORD.name, // table name
                Key.RECORD_ID.name + " = ?", // selection
                arrayOf(id.toString()) // selectionArgs
        )
    }

    // Getting SpendRecords Count
    fun getCount(table: Table): Int {
        val db = this.writableDatabase

        val countQuery = "SELECT  * FROM " + table.name

        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count

        cursor.close()

        // return count
        return count
    }

    fun getTypeId(typeName: String): Long {

        //if the type cannot be found, return this value
        var id: Long = ERROR_NOT_EXIST
        // Get writable database
        val db = this.writableDatabase
        val projection = arrayOf(Key.TYPE_ID.name, Key.TYPE_NAME.name)


        // Filter results WHERE "column_name" = 'value'
        // here selection is column_name and
        // selectionArgs is value
        val selection = Key.TYPE_NAME.name + "=?"
        val selectionArgs = arrayOf(typeName)

        // The order in which your result needs to be returned
        val sortOrder: String? = null // pass null if don't want it to be sorted

        // If passed 5, only 5 blogs will be returned
        val limit: String? = null // pass null if you dont want it to limit

        val cursor = db.query(
                Table.TYPE.name,        // The table to query
                projection,         // The columns to return
                selection,          // The columns for WHERE clause
                selectionArgs,      // The values for WHERE clause;
                null,        // don't group the rows
                null,         // don't filter
                sortOrder,          // The sort Order
                limit               // don't limit
        )

        cursor.let {
            if (cursor.moveToFirst()) {
                id = cursor.getString(0).toLong()
            }
        }
        cursor.close()
        return id
    }

    fun getAllSpendType(): List<String>? {

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        // SQL query for getting all records from the database
        val selectQuery = "SELECT  ${Key.TYPE_NAME.name} FROM ${Table.TYPE.name} WHERE ${Key.TYPE_NAME.name} <> '$SPECIAL_TYPE_INCOME'"
        val cursor = db.rawQuery(selectQuery, null)

        cursor.let {
            if (cursor.moveToFirst()) {
                val listOfType = arrayListOf<String>()
                do {
                    listOfType.add(cursor.getString(0))
                } while (cursor.moveToNext())

                cursor.close()

                return listOfType
            }
        }

        //return null if cursor return null
        return null
    }

    fun getBalance(): Double {
        //TODO: implement getBalance which returns the difference of total income and total spending
        val db = this.readableDatabase
        val incomeID = getTypeId(SPECIAL_TYPE_INCOME)
        val balanceQuery = """SELECT (total_income - total_spending) AS balance FROM
            |(SELECT IFNULL(SUM(${Key.RECORD_AMOUNT.name}), 0) AS total_income FROM ${Table.RECORD.name} T1 WHERE T1.${Key.RECORD_TYPE.name}=$incomeID),
            |(SELECT IFNULL(SUM(${Key.RECORD_AMOUNT.name}), 0) AS total_spending FROM ${Table.RECORD.name} T2 WHERE T2.${Key.RECORD_TYPE.name}<>$incomeID)
        """.trimMargin()
        val cursor = db.rawQuery(balanceQuery,null)
        cursor.let {
            if (cursor.moveToFirst()) {
                return cursor.getString(0).toDouble()
            }
        }
        return SQL_ERROR.toDouble()
    }

    fun addSpendType(type: String): Long {
        var returnVal: Long = ERROR_EXIST  //return error exist if type exists, return -1 for db error
        val db = this.writableDatabase
        // SQL query for getting all records from the database
        val selectQuery = "SELECT  * FROM ${Table.TYPE.name} WHERE ${Key.TYPE_NAME.name}='$type'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.count <= 0) {
            val values = ContentValues()
            values.put(Key.TYPE_NAME.name, type)

            returnVal = db.insert(Table.TYPE.name, null, values)
        }
        cursor.close()
        return returnVal
    }

    fun deleteSpendType(type_name: String) {
        val db = this.writableDatabase
        db.delete(
                Table.TYPE.name, // table name
                "${Key.TYPE_NAME.name} = '$type_name'", // selection
                null// selectionArgs
        )
    }


    fun clearTable(table: Table) {
        val db = this.writableDatabase

        val delSql = "DELETE  FROM ${table.name}"

        db.execSQL(delSql)
    }

    fun getData(Query: String): ArrayList<Cursor?> {
        //get writable database
        val sqlDB = this.writableDatabase
        val columns = arrayOf("message")
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        val alc = ArrayList<Cursor?>(2)
        val Cursor2 = MatrixCursor(columns)
        alc.add(null)
        alc.add(null)

        try {
//execute the query results will be save in Cursor c
            val c = sqlDB.rawQuery(Query, null)

            //add value to cursor2
            Cursor2.addRow(arrayOf<Any>("Success"))

            alc[1] = Cursor2
            if (null != c && c.count > 0) {

                alc[0] = c
                c.moveToFirst()

                return alc
            }
            return alc
        } catch (sqlEx: SQLException) {
            Log.d("printing exception", sqlEx.toString())
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(arrayOf<Any>("" + sqlEx.toString()))
            alc[1] = Cursor2
            return alc
        } catch (ex: Exception) {
            Log.d("printing exception", ex.message)

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(arrayOf<Any>("" + ex.message))
            alc[1] = Cursor2
            return alc
        }

    }
}