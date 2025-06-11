package com.nirmal.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class ResultActivity : AppCompatActivity() {

    private lateinit var resultImageView: ImageView
    private lateinit var resultLabel: TextView
    private lateinit var diseaseCharacteistics: TextView
    private lateinit var diseaseSymptoms: TextView
    private lateinit var diseaseRemedies: TextView
    private lateinit var diseaseTreatment: TextView
    private lateinit var diseasePrevention: TextView

    private var isMarathi: Boolean = true // Change this to control language

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultImageView = findViewById(R.id.resultImageView)
        resultLabel = findViewById(R.id.resultLabel)
        diseaseCharacteistics = findViewById(R.id.diseaseCharacteistics)
        diseaseSymptoms = findViewById(R.id.diseaseSymptoms)
        diseaseRemedies = findViewById(R.id.diseaseRemedies)
        diseaseTreatment = findViewById(R.id.diseaseTreatment)
        diseasePrevention = findViewById(R.id.diseasePrevention)

        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val selectedLanguage = sharedPreferences.getString("language", "English")

        isMarathi = selectedLanguage == "Marathi"

        val croppedImagePath = intent.getStringExtra("cropped_image_path")
        val predictedLabel = intent.getStringExtra("predicted_label")
        val characteristics = intent.getStringExtra("characteristics") ?: "Unknown"
        val symptoms = intent.getStringExtra("symptoms") ?: "Unknown"
        val chemicalTreatment = intent.getStringExtra("chemical_treatment") ?: "Unknown"
        val remedies = intent.getStringExtra("remedies") ?: "Unknown"
        val prevention = intent.getStringExtra("prevention") ?: "Unknown"

        if (croppedImagePath != null && predictedLabel != null) {
            Glide.with(this).load(croppedImagePath).into(resultImageView)
            resultLabel.text = "Prediction: $predictedLabel"

            if (isMarathi) {
                translateToMarathi(characteristics) { translatedCharacteristics ->
                    diseaseCharacteistics.text = makeBoldText("वैशिष्ट्ये:", translatedCharacteristics)
                }
                translateToMarathi(symptoms) { translatedSymptoms ->
                    diseaseSymptoms.text = makeBoldText("लक्षणे:", translatedSymptoms)
                }
                translateToMarathi(remedies) { translatedRemedies ->
                    diseaseRemedies.text = makeBoldText("उपाय:", translatedRemedies)
                }
                translateToMarathi(chemicalTreatment) { translatedTreatment ->
                    diseaseTreatment.text = makeBoldText("रासायनिक उपचार:", translatedTreatment)
                }
                translateToMarathi(prevention) { translatedPrevention ->
                    diseasePrevention.text = makeBoldText("प्रतिबंध:", translatedPrevention)
                }
            } else {
                diseaseCharacteistics.text = makeBoldText("Characteristics:", characteristics)
                diseaseSymptoms.text = makeBoldText("Symptoms:", symptoms)
                diseaseRemedies.text = makeBoldText("Remedies:", remedies)
                diseaseTreatment.text = makeBoldText("Chemical Treatment:", chemicalTreatment)
                diseasePrevention.text = makeBoldText("Prevention:", prevention)
            }
        } else {
            Toast.makeText(this, "Error displaying results", Toast.LENGTH_SHORT).show()
        }
    }


    // Function to translate text using ML Kit
    private fun translateToMarathi(text: String, callback: (String) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.MARATHI)
            .build()
        val translator: Translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        callback(translatedText)
                    }
                    .addOnFailureListener {
                        callback(text) // If translation fails, use the original text
                    }
            }
            .addOnFailureListener {
                callback(text) // If model download fails, use the original text
            }
    }

    // Function to make label bold and place value on the next line
    private fun makeBoldText(label: String, value: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()
        spannable.append(label).append("\n").append(value)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
}
