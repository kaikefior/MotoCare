package com.example.motocare

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.motocare.databinding.ActivityMotoListBinding
import com.example.motocare.session.SessionManager
import com.example.motocare.ui.MotoListFragment
import com.example.motocare.ui.aplicarInsets

class MotoListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)
        if (session.exigirUsuario(this) == null) return
        enableEdgeToEdge()
        val binding = ActivityMotoListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        binding.toolbar.inflateMenu(R.menu.menu_list)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_logout) {
                session.sair(this)
                true
            } else false
        }
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.motoContainer, MotoListFragment())
            }
        }
    }
}
