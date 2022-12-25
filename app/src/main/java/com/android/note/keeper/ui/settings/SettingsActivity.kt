package com.android.note.keeper.ui.settings

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.android.note.keeper.R
import com.android.note.keeper.databinding.SettingsActivityBinding
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding:SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //todo


    }

}