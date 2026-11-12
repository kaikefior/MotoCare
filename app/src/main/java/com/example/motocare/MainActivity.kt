package com.example.motocare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.motocare.database.DatabaseHelper
import com.example.motocare.databinding.ActivityMainBinding
import com.example.motocare.model.Usuario
import com.example.motocare.session.SessionManager
import com.example.motocare.ui.aplicarInsets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)
        if (session.usuarioId != null) {
            abrirMotos()
            return
        }
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        binding.btnLogin.setOnClickListener { entrar() }
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun entrar() {
        val email = binding.etEmail.text.toString().trim().lowercase(Locale.ROOT)
        val senha = binding.etPassword.text.toString()
        if (email.isBlank() || senha.isBlank()) {
            Toast.makeText(this, R.string.fill_fields_error, Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnLogin.isEnabled = false
        binding.btnSignup.isEnabled = false
        lifecycleScope.launch {
            try {
                val usuario = withContext(Dispatchers.IO) {
                    DatabaseHelper(applicationContext).use { db ->
                        db.autenticarUsuario(email, senha) ?: if (
                            email == "admin@admin.com" && senha == "123456" && !db.existeUsuario(email)
                        ) {
                            // O usuário de demonstração também precisa de um ID real.
                            val demo = Usuario(nome = "Demonstração", email = email, senha = senha)
                            val id = db.cadastrarUsuario(demo)
                            if (id > 0) demo.copy(id = id) else null
                        } else null
                    }
                }
                if (usuario == null) {
                    Toast.makeText(this@MainActivity, R.string.invalid_login, Toast.LENGTH_SHORT).show()
                } else {
                    session.entrar(usuario.id)
                    abrirMotos()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MotoCare", "Falha no login", e)
                Toast.makeText(this@MainActivity, R.string.database_error, Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnSignup.isEnabled = true
            }
        }
    }

    private fun abrirMotos() {
        startActivity(Intent(this, MotoListActivity::class.java))
        finish()
    }
}
