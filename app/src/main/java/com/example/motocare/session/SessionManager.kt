package com.example.motocare.session

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.motocare.MainActivity

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("motocare_session", Context.MODE_PRIVATE)

    val usuarioId: Long?
        get() = preferences.getLong("usuario_id", -1L).takeIf { it > 0 }

    fun entrar(usuarioId: Long) {
        require(usuarioId > 0)
        preferences.edit().putLong("usuario_id", usuarioId).apply()
    }

    fun exigirUsuario(activity: Activity): Long? {
        val id = usuarioId
        if (id == null) sair(activity)
        return id
    }

    fun sair(activity: Activity) {
        preferences.edit().clear().apply()
        activity.startActivity(Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        activity.finish()
    }
}
