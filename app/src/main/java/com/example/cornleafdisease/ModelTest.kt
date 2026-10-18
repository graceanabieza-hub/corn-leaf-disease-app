package com.example.cornleafdisease

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ModelTest {

    fun testAnfis(context: Context) {
        try {
            val jsonText = context.assets.open(
                "mobilenetv2_anfis_parameters.json"
            ).bufferedReader().use { it.readText() }

            val anfis = GeneralizedBellInference(
                org.json.JSONObject(jsonText)
            )

            // Temporary test input only
            val testFeatures = FloatArray(8) { 0.5f }

            val (disease, probabilities) =
                anfis.predict(testFeatures)

            Log.d("ANFIS_TEST", "ANFIS calculation successful!")
            Log.d("ANFIS_TEST", "Test output: $disease")
            Log.d(
                "ANFIS_TEST",
                "Probabilities: ${probabilities.contentToString()}"
            )
            Log.d(
                "ANFIS_TEST",
                "Probability sum: ${probabilities.sum()}"
            )

        } catch (e: Exception) {
            Log.e("ANFIS_TEST", "ANFIS calculation failed", e)
        }
    }
    fun test(context: Context) {
        try {
            val jsonText = context.assets.open(
                "mobilenetv2_anfis_parameters.json"
            ).bufferedReader().use { it.readText() }

            val json = org.json.JSONObject(jsonText)

            Log.d(
                "ANFIS_TEST",
                "ANFIS JSON loaded successfully!"
            )

            Log.d(
                "ANFIS_TEST",
                "JSON keys: ${json.keys().asSequence().toList()}"
            )

        } catch (e: Exception) {
            Log.e(
                "ANFIS_TEST",
                "Failed to load ANFIS JSON",
                e
            )
        }
        try {
            Log.d("MODEL_TEST", "Loading MobileNet TFLite...")

            // Load actual TFLite model from assets
            val modelBytes = context.assets.open(
                "mobilenetv2_eight_features.tflite"
            ).use { it.readBytes() }

            val modelBuffer = ByteBuffer
                .allocateDirect(modelBytes.size)
                .order(ByteOrder.nativeOrder())

            modelBuffer.put(modelBytes)
            modelBuffer.rewind()

            // Initialize TFLite interpreter
            Interpreter(modelBuffer).use { interpreter ->

                val inputShape =
                    interpreter.getInputTensor(0).shape()

                val outputShape =
                    interpreter.getOutputTensor(0).shape()

                Log.d(
                    "MODEL_TEST",
                    "Model loaded successfully!"
                )

                Log.d(
                    "MODEL_TEST",
                    "Input: ${inputShape.contentToString()}"
                )

                Log.d(
                    "MODEL_TEST",
                    "Output: ${outputShape.contentToString()}"
                )

                // Verify expected shapes
                if (
                    inputShape.contentEquals(
                        intArrayOf(1, 3, 224, 224)
                    ) &&
                    outputShape.contentEquals(
                        intArrayOf(1, 8)
                    )
                ) {
                    Log.d(
                        "MODEL_TEST",
                        "PASS: MobileNetV3 model is ready!"
                    )
                } else {
                    Log.e(
                        "MODEL_TEST",
                        "Unexpected model input/output shapes!"
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(
                "MODEL_TEST",
                "Failed to load MobileNetV3 model",
                e
            )
        }
    }
}