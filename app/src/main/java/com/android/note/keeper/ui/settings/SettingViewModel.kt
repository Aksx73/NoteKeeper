package com.android.note.keeper.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.note.keeper.data.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isPasswordVisible = MutableLiveData<Boolean>(false)
    val isPasswordVisible : LiveData<Boolean> = _isPasswordVisible

    val themeFlow = preferenceManager.themeMode.asLiveData()
    val dynamicColorFlow = preferenceManager.dynamicThemingFlow.asLiveData()

    //here both are needed
    val masterPasswordFlow = preferenceManager.masterPasswordFlow
    val masterPasswordLiveData = preferenceManager.masterPasswordFlow.asLiveData()

    fun setMasterPassword(password: String) = viewModelScope.launch {
        preferenceManager.setMasterPassword(password)
    }

    fun setThemeMode(uiMode: Int) = viewModelScope.launch {
        preferenceManager.setThemeMode(uiMode)
    }

    fun setDynamicColorEnabled(isEnabled: Boolean) = viewModelScope.launch {
        preferenceManager.setDynamicTheming(isEnabled)
    }

    fun setIsPasswordVisible(bool:Boolean){
        _isPasswordVisible.value = bool
    }

    fun switchIsPasswordVisible(){
        _isPasswordVisible.value?.let {
            _isPasswordVisible.value = !it
        }
    }

}