package com.android.note.keeper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.note.keeper.BuildConfig
import com.android.note.keeper.R
import com.android.note.keeper.databinding.ActivityAboutBinding
import com.android.note.keeper.databinding.SettingsActivityBinding
import com.google.android.material.color.DynamicColors

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
    }

    private fun initViews() {

       //todo
        binding.txtAppVersion.text = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
    }


}