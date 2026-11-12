package com.example.motocare.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.motocare.model.Usuario

// Este banco atende apenas ao login. As motos são salvas pelo Room.
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "motocare.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                senha TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Ao aumentar a versão, implementar uma migração para preservar os cadastros.
        error("Migração do banco de usuários ainda não definida.")
    }

    fun cadastrarUsuario(usuario: Usuario): Long {
        val values = ContentValues().apply {
            put("nome", usuario.nome)
            put("email", usuario.email)
            put("senha", usuario.senha)
        }
        return writableDatabase.insert("usuarios", null, values)
    }

    fun autenticarUsuario(email: String, senha: String): Usuario? {
        readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE email COLLATE NOCASE = ? AND senha = ?",
            arrayOf(email, senha)
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return Usuario(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                senha = cursor.getString(cursor.getColumnIndexOrThrow("senha"))
            )
        }
    }

    fun existeUsuario(email: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM usuarios WHERE email COLLATE NOCASE = ? LIMIT 1",
            arrayOf(email)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }
}
