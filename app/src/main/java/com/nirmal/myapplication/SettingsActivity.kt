package com.nirmal.myapplication

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioEnglish: RadioButton
    private lateinit var radioMarathi: RadioButton
    private lateinit var editUploadUrl: EditText
    private lateinit var btnSave: Button

    private lateinit var sharedPreferences: SharedPreferences

    private val DEFAULT_UPLOAD_URL = "http://192.168.42.37:5000/upload" // Set your default URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        radioGroup = findViewById(R.id.languageRadioGroup)
        radioEnglish = findViewById(R.id.radioEnglish)
        radioMarathi = findViewById(R.id.radioMarathi)
        editUploadUrl = findViewById(R.id.editAdditionalInfo) // Renaming to indicate upload URL
        btnSave = findViewById(R.id.btnSave)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Load saved settings
        loadSettings()

        btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        val selectedLanguage = if (radioMarathi.isChecked) "Marathi" else "English"
        val uploadUrl = editUploadUrl.text.toString().trim()

        val editor = sharedPreferences.edit()
        editor.putString("language", selectedLanguage)
        editor.putString("upload_url", if (uploadUrl.isEmpty()) DEFAULT_UPLOAD_URL else uploadUrl)
        editor.apply()

        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show()
    }

    private fun loadSettings() {
        val language = sharedPreferences.getString("language", "English")
        val uploadUrl = sharedPreferences.getString("upload_url", DEFAULT_UPLOAD_URL)

        if (language == "Marathi") {
            radioMarathi.isChecked = true
        } else {
            radioEnglish.isChecked = true
        }

        editUploadUrl.setText(uploadUrl)
    }
}
