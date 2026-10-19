package com.example.cornleafdisease

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CornLeafClassifier(private val context: Context) : Closeable {
    data class ClassificationResult(
        val disease: String,
        val confidence: Double,
        val probabilities: DoubleArray
    )

    private val interpreter: Interpreter
    private val anfis: GeneralizedBellInference
    private val imageSize = 224
    private val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val std = floatArrayOf(0.229f, 0.224f, 0.225f)

    init {
        val modelBytes = context.assets.open("mobilenetv2_eight_features_float32.tflite").use { it.readBytes() }
        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
        modelBuffer.put(modelBytes)
        modelBuffer.rewind()
        interpreter = Interpreter(modelBuffer)
        val json = context.assets.open("model_parameters.json").bufferedReader().use { JSONObject(it.readText()) }
        anfis = GeneralizedBellInference(json)
        val shape = interpreter.getInputTensor(0).shape()
        require(shape.contentEquals(intArrayOf(1, 224, 224, 3))) {
            "Expected NHWC [1,224,224,3], got ${shape.contentToString()}"
        }
        val outputShape = interpreter.getOutputTensor(0).shape()
        require(outputShape.contentEquals(intArrayOf(1, 8))) {
            "Expected [1,8] CNN features, got ${outputShape.contentToString()}"
        }
        Log.d("CORN_CLASSIFIER", "Five-class CNN–ANFIS initialized")
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        val input = preprocess(bitmap)
        val features = Array(1) { FloatArray(8) }
        synchronized(interpreter) { interpreter.run(input, features) }
        Log.d("CORN_DIAG", "CNN features: ${features[0].contentToString()}")
        val (rawLabel, probabilities, logits) = anfis.predictWithLogits(features[0])
        Log.d("CORN_DIAG", "ANFIS logits: ${logits.contentToString()}")
        Log.d("CORN_DIAG", "Class probabilities: ${probabilities.contentToString()}")
        val confidence = probabilities.maxOrNull() ?: 0.0
        val displayLabel = if (rawLabel.equals("Non-Corn Leaf", ignoreCase = true)) {
            "Corn Leaf Not Detected"
        } else rawLabel
        return ClassificationResult(displayLabel, confidence, probabilities)
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        val pixels = IntArray(imageSize * imageSize)
        resized.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)
        val buffer = ByteBuffer.allocateDirect(1 * imageSize * imageSize * 3 * 4)
            .order(ByteOrder.nativeOrder())
        for (pixel in pixels) {
            buffer.putFloat((Color.red(pixel) / 255f - mean[0]) / std[0])
            buffer.putFloat((Color.green(pixel) / 255f - mean[1]) / std[1])
            buffer.putFloat((Color.blue(pixel) / 255f - mean[2]) / std[2])
        }
        buffer.rewind()
        if (resized !== bitmap) resized.recycle()
        return buffer
    }

    override fun close() = interpreter.close()
}
