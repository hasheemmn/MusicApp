package com.oges.myapplication

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.oges.myapplication.databinding.ActivityMainBinding
import com.oges.myapplication.fragment.HomeFragment
import com.oges.myapplication.utils.ConfirmationBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragmentActivityMain) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top, // Top for status bar
                systemBars.right,
                ime.bottom // Only keyboard pushes content
            )
            insets
        }
        setStatusBar(binding.root.context.getColor(R.color._F0CCCF), true)
    }

    fun setStatusBar(color: Int, isLightMode: Boolean) {
        window.statusBarColor = color
        binding.IdMainLayout.setBackgroundColor(color)
        if (isLightMode) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = 0
        }
    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        if (currentFragment is HomeFragment) {
            val bottomSheet = ConfirmationBottomSheet("Exit", "exit app", {
                finishAffinity()
            }, {
            })
            bottomSheet.show(supportFragmentManager, "")
        } else {
            super.onBackPressed()
        }
    }





}