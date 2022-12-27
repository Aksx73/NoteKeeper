package com.android.note.keeper.ui.settings

import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.android.note.keeper.R
import com.android.note.keeper.databinding.SettingsActivityBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding
    private val viewModel by viewModels<SettingViewModel>()

    private lateinit var masterPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadMasterPassword()

        //used this to update masterPassword value with updated password
        viewModel.masterPasswordLiveData.observe(this) {
            masterPassword = it
            if (it.isNotBlank()) binding.content.txtCurrentPassword.text = it
            else binding.content.txtCurrentPassword.text = "no current password"
            Log.d("TAG", "mastPassword live: $masterPassword")
        }

        viewModel.isPasswordVisible.observe(this) {
            eyeUI(it)
        }

        initViews()
    }

    private fun loadMasterPassword() {
        //used this to get masterPassword value immediately
        this.lifecycleScope.launch {
            masterPassword = viewModel.masterPasswordFlow.first()
            Log.d("TAG", "masterPasswordFLow: $masterPassword")
        }
    }


    private fun initViews() {
        binding.apply {
            content.lytChangeMasterPassword.setOnClickListener {
                bottomSheetUpdateMasterPassword()
            }
            content.imgShowHidePassword.setOnClickListener {
                viewModel.switchIsPasswordVisible()
            }
            content.txtCurrentPassword.text = ""
        }

    }

    private fun eyeUI(isPasswordVisible: Boolean) {
        if (!isPasswordVisible) {
            binding.content.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imgShowHidePassword.tooltipText = "Show password"
                }
                imgShowHidePassword.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_remove_eye_24,
                        null
                    )
                )
                txtCurrentPassword.transformationMethod = PasswordTransformationMethod()
            }
        } else {
            binding.content.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imgShowHidePassword.tooltipText = "Hide password"
                }
                imgShowHidePassword.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_visibility_off_24,
                        null
                    )
                )
                txtCurrentPassword.transformationMethod = HideReturnsTransformationMethod()
            }
        }
    }

    private fun bottomSheetUpdateMasterPassword() {
        val bottomSheetDialog = BottomSheetDialog(this@SettingsActivity)
        val bottomsheet: View =
            LayoutInflater.from(this@SettingsActivity).inflate(R.layout.bs_create_password, null)
        bottomSheetDialog.setCancelable(false)

        val title = bottomsheet.findViewById<TextView>(R.id.txtTitle)
        val subtitle = bottomsheet.findViewById<TextView>(R.id.txtSubTitle)
        val et_currentPassword =
            bottomsheet.findViewById<TextInputEditText>(R.id.et_currentPassword)
        val ly_currentPassword = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_currentPassword)
        val et_password = bottomsheet.findViewById<TextInputEditText>(R.id.et_addPassword)
        val ly_password = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_addPassword)
        val et_confirm = bottomsheet.findViewById<TextInputEditText>(R.id.et_confirmPassword)
        val ly_confirm = bottomsheet.findViewById<TextInputLayout>(R.id.lyt_confirmPassword)
        val bt_save = bottomsheet.findViewById<MaterialButton>(R.id.bt_save)
        val bt_cancel = bottomsheet.findViewById<MaterialButton>(R.id.bt_cancel)

        if (masterPassword.isNotBlank()) { //update master password
            ly_currentPassword.isVisible = true
            title.text = "Update master password"
            subtitle.text =
                "Confirm your current master password and then create a new one"
            bt_save.text = "Update"

            bt_save.setOnClickListener {
                if (et_currentPassword.text.toString() == masterPassword) {
                    // correct
                    if (et_password.text.toString().isNotBlank() && et_confirm.text.toString().isNotBlank()) {
                        if (et_password.text.toString() == et_confirm.text.toString()) {
                            //todo save to datastore
                            viewModel.setMasterPassword(et_password.text.toString())
                            masterPassword = et_password.text.toString()
                            Snackbar.make(
                                binding.anchor,
                                "Master password updated!",
                                Snackbar.LENGTH_LONG
                            ).show()
                            loadMasterPassword()

                            bottomSheetDialog.dismiss()
                        } else { //password not match
                            ly_confirm.isErrorEnabled = true
                            ly_confirm.error = "New password doesn't match"
                        }
                    } else { // show error for edit text
                        ly_confirm.isErrorEnabled = true
                        ly_confirm.error = "Re-enter new password here"
                    }
                } else {
                    ly_currentPassword.isErrorEnabled = true
                    ly_currentPassword.error = "Current password doesn't match"
                }
            }

        } else { //create master password
            ly_currentPassword.isVisible = false
            title.text = "Create master password"
            subtitle.text =
                "Remember this password as you will need to open note whose password protection is enabled."
            bt_save.text = "Create"

            bt_save.setOnClickListener {
                if (et_password.text.toString().isNotBlank() && et_confirm.text.toString()
                        .isNotBlank()
                ) {
                    if (et_password.text.toString() == et_confirm.text.toString()) {
                        //todo save to datastore
                        viewModel.setMasterPassword(et_password.text.toString())
                        masterPassword = et_password.text.toString()
                        Snackbar.make(
                            binding.anchor,
                            "Master password added!",
                            Snackbar.LENGTH_LONG
                        ).show()
                        loadMasterPassword()
                        viewModel.setIsPasswordVisible(false)
                        bottomSheetDialog.dismiss()
                    } else { //password not match
                        ly_confirm.isErrorEnabled = true
                        ly_confirm.error = "Password doesn't match"
                    }
                } else { // show error for edit text
                    ly_confirm.isErrorEnabled = true
                    ly_confirm.error = "Re-enter password here"
                }
            }

        }

        et_confirm.doOnTextChanged { text, start, before, count ->
            ly_confirm.isErrorEnabled = false
            ly_confirm.error = null
        }

        et_currentPassword.doOnTextChanged { text, start, before, count ->
            ly_currentPassword.isErrorEnabled = false
            ly_currentPassword.error = null
        }

        bt_cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomsheet)
        bottomSheetDialog.show()
    }


}