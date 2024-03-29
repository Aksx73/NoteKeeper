package com.android.note.keeper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.note.keeper.databinding.ActivityAboutBinding
import com.android.note.keeper.databinding.ActivityBackupBinding
import com.google.android.material.color.DynamicColors

class BackupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
    }

    private fun initViews() {
       // TODO
    }
}