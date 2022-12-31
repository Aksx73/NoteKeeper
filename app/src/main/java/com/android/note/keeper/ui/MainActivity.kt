package com.android.note.keeper.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.android.note.keeper.R
import com.android.note.keeper.databinding.ActivityMain2Binding
import com.android.note.keeper.ui.settings.SettingsActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding
    lateinit var toolbar: MaterialToolbar
    lateinit var readMode: TextView

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        readMode = binding.currentMode

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.NoteListFragment,
                R.id.ArchiveNoteFragment,
                R.id.DeletedNoteFragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        initDrawer()

        //replacement for deprecated onBackPressed()
       /* onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })*/

    }

    private fun initDrawer() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.navigationView.setCheckedItem(destination.id)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // binding.navigationView.setCheckedItem(menuItem)
            when (menuItem.itemId) {
                R.id.backup -> {
                    Snackbar.make(binding.drawerLayout, "Backup clicked", Snackbar.LENGTH_SHORT)
                        .show()
                    binding.drawerLayout.close()
                }
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    // binding.drawerLayout.close()
                }
                R.id.about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawers()
                }
            }
            true
        }

    }

    override fun onResume() {
        super.onResume()
        //todo add addOnDestinationChangedListener here
    }

    override fun onDestroy() {
        super.onDestroy()
        //todo remove addOnDestinationChangedListener here
    }

     @Deprecated("Deprecated in Java")
     override fun onBackPressed() {
         if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
             binding.drawerLayout.closeDrawer(GravityCompat.START)
         } else {
             super.onBackPressed()
         }
     }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}