package com.example.cornleafdisease

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.*

/** Inference for the exported five-class Generalized Bell ANFIS checkpoint. */
class GeneralizedBellInference(private val json: JSONObject) {
    private fun vector(a: JSONArray) = DoubleArray(a.length()) { a.getDouble(it) }
    private fun matrix(a: JSONArray) = Array(a.length()) { vector(a.getJSONArray(it)) }

    private val labels = json.getJSONArray("class_names")
    private val scalerMin = vector(json.getJSONArray("scaler_min"))
    private val scalerScale = vector(json.getJSONArray("scaler_scale"))
    private val parameters = json.getJSONObject("anfis")
    private val centers = matrix(parameters.getJSONArray("centers"))
    private val rawWidths = matrix(parameters.getJSONArray("raw_widths"))
    private val rawSlopes = matrix(parameters.getJSONArray("raw_slopes"))
    private val ruleIndices = matrix(parameters.getJSONArray("rule_indices"))
    private val consequents = parameters.getJSONArray("consequents")

    init {
        require(scalerMin.size == scalerScale.size)
        require(centers.size == scalerMin.size)
        require(ruleIndices.isNotEmpty())
        require(consequents.length() == ruleIndices.size)
        require(labels.length() == 5) { "Expected 5 output classes" }
    }

    private fun softplus(v: Double): Double = max(v, 0.0) + ln1p(exp(-abs(v)))

    fun predict(features: FloatArray): Pair<String, DoubleArray> {
        val (label, probabilities, _) = predictWithLogits(features)
        return label to probabilities
    }

    fun predictWithLogits(features: FloatArray): Triple<String, DoubleArray, FloatArray> {
        require(features.size == scalerMin.size) { "Expected ${scalerMin.size} features, got ${features.size}" }
        require(features.all { it.isFinite() }) { "Non-finite CNN features" }
        val x = DoubleArray(features.size) { i -> features[i].toDouble() * scalerScale[i] + scalerMin[i] }
        val logFiring = DoubleArray(ruleIndices.size)

        for (r in ruleIndices.indices) {
            var logStrength = 0.0
            for (i in x.indices) {
                val mf = ruleIndices[r][i].toInt()
                val width = softplus(rawWidths[i][mf]) + 1e-4
                val slope = softplus(rawSlopes[i][mf]) + 1e-4
                val ratio = max(abs((x[i] - centers[i][mf]) / width), 1e-8)
                // Stable log(1 / (1 + ratio^(2*slope)))
                logStrength -= softplus(2.0 * slope * ln(ratio))
            }
            logFiring[r] = logStrength
        }

        val maxLog = logFiring.maxOrNull()!!
        val strengths = DoubleArray(ruleIndices.size) { exp(logFiring[it] - maxLog) }
        val totalStrength = strengths.sum()
        require(totalStrength > 0.0 && totalStrength.isFinite()) { "Invalid ANFIS firing strengths" }

        val logits = DoubleArray(labels.length())
        for (r in ruleIndices.indices) {
            val weight = strengths[r] / totalStrength
            val coeff = consequents.getJSONArray(r) // [features + bias][classes]
            for (k in logits.indices) {
                var output = coeff.getJSONArray(x.size).getDouble(k)
                for (i in x.indices) output += x[i] * coeff.getJSONArray(i).getDouble(k)
                logits[k] += weight * output
            }
        }

        // Uncalibrated softmax: do not artificially inflate displayed confidence.
        val maxValue = logits.maxOrNull()!!
        val probabilities = DoubleArray(logits.size) { exp(logits[it] - maxValue) }
        val sum = probabilities.sum()
        require(sum > 0.0 && sum.isFinite()) { "Invalid ANFIS probabilities" }
        for (i in probabilities.indices) probabilities[i] /= sum
        val winner = probabilities.indices.maxByOrNull { probabilities[it] }!!
        return Triple(labels.getString(winner), probabilities, FloatArray(logits.size) { logits[it].toFloat() })
    }
}