package com.floki.gxpence

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "chatDatabase"
        private const val TABLE_MESSAGES = "chatMessages"
        private const val KEY_ID = "id"
        private const val KEY_MESSAGE = "message"
        private const val KEY_IS_USER = "isUser"
        private const val KEY_SESSION_ID = "sessionId"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_MESSAGES_TABLE = ("CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE + " TEXT,"
                + KEY_IS_USER + " BOOLEAN," + KEY_SESSION_ID + " TEXT" + ")")
        db?.execSQL(CREATE_MESSAGES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        onCreate(db)
    }

    fun addMessage(chatMessage: ChatMessage): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_MESSAGE, chatMessage.message)
        contentValues.put(KEY_IS_USER, chatMessage.isUser)
        contentValues.put(KEY_SESSION_ID, chatMessage.sessionId)
        val success = db.insert(TABLE_MESSAGES, null, contentValues)
        db.close()
        return success
    }

    @SuppressLint("Range")
    fun getMessages(): List<ChatMessage> {
        val chatMessages = mutableListOf<ChatMessage>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MESSAGES", null)
        if (cursor.moveToFirst()) {
            do {
                val message = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE))
                val isUser = cursor.getInt(cursor.getColumnIndex(KEY_IS_USER)) != 0
                val sessionId = cursor.getString(cursor.getColumnIndex(KEY_SESSION_ID))
                chatMessages.add(ChatMessage(message, isUser, sessionId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return chatMessages
    }

    fun clearMessages() {
        val db = this.writableDatabase
        db.delete(TABLE_MESSAGES, null, null)
        db.close()
    }
}
