package com.example.motocare

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.motocare.database.DatabaseHelper
import com.example.motocare.databinding.ActivityRegisterBinding
import com.example.motocare.model.Usuario
import com.example.motocare.ui.aplicarInsets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        binding.btnDoRegister.setOnClickListener { cadastrar() }
        binding.tvBackToLogin.setOnClickListener { finish() }
    }

    private fun cadastrar() {
        val nome = binding.etRegNome.text.toString().trim()
        val email = binding.etRegEmail.text.toString().trim().lowercase(Locale.ROOT)
        val senha = binding.etRegPassword.text.toString()
        binding.tilRegNome.error = if (nome.isBlank()) getString(R.string.required_field) else null
        binding.tilRegEmail.error = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) getString(R.string.invalid_email) else null
        binding.tilRegPassword.error = if (senha.isBlank() || senha.length < 6) getString(R.string.invalid_password) else null
        if (listOf(binding.tilRegNome, binding.tilRegEmail, binding.tilRegPassword).any { it.error != null }) return

        binding.btnDoRegister.isEnabled = false
        lifecycleScope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    DatabaseHelper(applicationContext).use { db ->
                        if (db.existeUsuario(email)) -2L
                        else db.cadastrarUsuario(Usuario(nome = nome, email = email, senha = senha))
                    }
                }
                when {
                    id == -2L -> binding.tilRegEmail.error = getString(R.string.user_exists_error)
                    id > 0 -> {
                        Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> Toast.makeText(this@RegisterActivity, R.string.database_error, Toast.LENGTH_LONG).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MotoCare", "Falha no cadastro de usuário", e)
                Toast.makeText(this@RegisterActivity, R.string.database_error, Toast.LENGTH_LONG).show()
            } finally {
                binding.btnDoRegister.isEnabled = true
            }
        }
    }
}
