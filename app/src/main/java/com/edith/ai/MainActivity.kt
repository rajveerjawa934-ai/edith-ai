
package com.edith.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "EDITH"
            textSize = 36f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "नमस्ते! मैं EDITH हूँ।"
            textSize = 20f
            gravity = Gravity.CENTER
        }

        resultText = TextView(this).apply {
            text = "मुझसे बात करने के लिए माइक्रोफोन दबाएँ।"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val micButton = Button(this).apply {
            text = "🎤 बात करें"
            textSize = 20f
            setOnClickListener {
                startListening()
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(resultText)
        layout.addView(micButton)

        setContentView(layout)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "इस फोन में Speech Recognition उपलब्ध नहीं है।"
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                statusText.text = "सुन रही हूँ..."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                val spokenText = matches?.firstOrNull() ?: "कुछ सुनाई नहीं दिया।"
                resultText.text = "आपने कहा: $spokenText"

                val reply = "नमस्ते! आपने कहा $spokenText"
                statusText.text = reply
                tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "edith_reply")
            }

            override fun onError(error: Int) {
                statusText.text = "आवाज़ समझ नहीं आई। दोबारा कोशिश करें।"
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
        }

        speechRecognizer?.startListening(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
