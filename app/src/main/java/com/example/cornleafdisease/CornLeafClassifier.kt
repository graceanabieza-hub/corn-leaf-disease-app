package com.example.cornleafdisease


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CornLeafClassifier(
    private val context: Context
) : Closeable {

    data class ClassificationResult(
        val disease: String,
        val confidence: Double,
        val probabilities: DoubleArray
    )

    private val interpreter: Interpreter
    private val anfis: GeneralizedBellInference

    init {
        // Load MobileNetV3 TFLite model
        val modelBytes = context.assets.open(
            "mobilenetv2_eight_features.tflite"
        ).use { it.readBytes() }

        val modelBuffer = ByteBuffer.allocateDirect(
            modelBytes.size
        ).order(ByteOrder.nativeOrder())

        modelBuffer.put(modelBytes)
        modelBuffer.rewind()

        interpreter = Interpreter(modelBuffer)

        // Load trained ANFIS parameters
        val jsonText = context.assets.open(
            "mobilenetv2_anfis_parameters.json"
        ).bufferedReader().use { it.readText() }

        anfis = GeneralizedBellInference(
            JSONObject(jsonText)
        )

        Log.d("CORN_CLASSIFIER", "CNN + ANFIS initialized")
    }

    fun classify(bitmap: Bitmap): ClassificationResult {

        // Prepare 224x224 image
        val input = preprocess(bitmap)

        // Extract eight learned CNN features
        val features = Array(1) {
            FloatArray(8)
        }

        synchronized(interpreter) {
            interpreter.run(input, features)
        }

        Log.d(
            "CORN_CLASSIFIER",
            "CNN features: ${features[0].contentToString()}"
        )

        // Generalized Bell ANFIS classification
        val (disease, probabilities) =
            anfis.predict(features[0])

        Log.d(
            "CORN_CLASSIFIER",
            "ANFIS raw prediction: $disease, probabilities: ${probabilities.contentToString()}"
        )

        val confidence = probabilities.maxOrNull() ?: 0.0

        Log.d(
            "CORN_CLASSIFIER",
            "Disease: $disease, confidence: $confidence"
        )

        return ClassificationResult(
            disease = disease,
            confidence = confidence,
            probabilities = probabilities
        )
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {

        val resized = Bitmap.createScaledBitmap(
            bitmap,
            224,
            224,
            true
        )

        val pixels = IntArray(224 * 224)

        resized.getPixels(
            pixels, 0, 224,
            0, 0, 224, 224
        )

        val inputShape = interpreter.getInputTensor(0).shape()
        val isNCHW = inputShape.size == 4 && inputShape[1] == 3

        Log.d("CORN_CLASSIFIER", "Model input shape: ${inputShape.contentToString()} (isNCHW=$isNCHW)")

        val buffer = ByteBuffer.allocateDirect(
            1 * 3 * 224 * 224 * 4
        ).order(ByteOrder.nativeOrder())

        // ImageNet normalization
        val mean = floatArrayOf(
            0.485f, 0.456f, 0.406f
        )

        val std = floatArrayOf(
            0.229f, 0.224f, 0.225f
        )

        // Contrast stretching / Histogram normalization (CLAHE approximation)
        var minLum = 255
        var maxLum = 0
        for (pixel in pixels) {
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
            if (lum < minLum) minLum = lum
            if (lum > maxLum) maxLum = lum
        }
        val lumRange = if (maxLum > minLum) (maxLum - minLum).toFloat() else 255f

        fun stretch(v: Int): Float {
            return ((v - minLum) * 255f / lumRange).coerceIn(0f, 255f)
        }

        if (isNCHW) {
            // NCHW: [1, 3, 224, 224]
            for (channel in 0 until 3) {
                for (pixel in pixels) {
                    val rawVal = when (channel) {
                        0 -> android.graphics.Color.red(pixel)
                        1 -> android.graphics.Color.green(pixel)
                        else -> android.graphics.Color.blue(pixel)
                    }
                    val value = stretch(rawVal)
                    val normalized = (value / 255.0f - mean[channel]) / std[channel]
                    buffer.putFloat(normalized)
                }
            }
        } else {
            // NHWC: [1, 224, 224, 3] (Standard TFLite)
            for (pixel in pixels) {
                val r = (stretch(android.graphics.Color.red(pixel)) / 255.0f - mean[0]) / std[0]
                val g = (stretch(android.graphics.Color.green(pixel)) / 255.0f - mean[1]) / std[1]
                val b = (stretch(android.graphics.Color.blue(pixel)) / 255.0f - mean[2]) / std[2]
                buffer.putFloat(r)
                buffer.putFloat(g)
                buffer.putFloat(b)
            }
        }

        buffer.rewind()
        return buffer
    }

    override fun close() {
        interpreter.close()
    }
}
