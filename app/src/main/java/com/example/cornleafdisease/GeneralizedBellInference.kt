package com.example.cornleafdisease

// Pure Kotlin Generalized Bell ANFIS inference. Input: eight features from TFLite.
// Requires org.json (built into Android).
import org.json.JSONObject
import kotlin.math.*

class GeneralizedBellInference(private val j: JSONObject) {
    private fun vec(a: org.json.JSONArray): DoubleArray = DoubleArray(a.length()) { a.getDouble(it) }
    private fun matrix(a: org.json.JSONArray): Array<DoubleArray> = Array(a.length()) { vec(a.getJSONArray(it)) }
    private val names = j.getJSONArray("class_names")
    private val mins = vec(j.getJSONArray("scaler_min"))
    private val scales = vec(j.getJSONArray("scaler_scale"))
    private val centers = matrix(j.getJSONArray("centers"))
    private val widths = matrix(j.getJSONArray("raw_widths"))
    private val slopes = matrix(j.getJSONArray("raw_slopes"))
    private val rules = matrix(j.getJSONArray("rule_indices"))
    private val cons = j.getJSONArray("consequents")
    private fun softplus(x: Double): Double = max(x,0.0)+ln1p(exp(-abs(x)))
    fun predict(features: FloatArray): Pair<String, DoubleArray> {
        require(features.size == mins.size) { "Expected ${mins.size} features" }
        val x = DoubleArray(features.size) { features[it].toDouble()*scales[it]+mins[it] }
        val logits = DoubleArray(names.length())
        val logFiring = DoubleArray(rules.size)
        for (r in rules.indices) {
            var logStrength = 0.0
            for (i in x.indices) {
                val m = rules[r][i].toInt()

                val w = softplus(widths[i][m]) + 1e-4
                val b = softplus(slopes[i][m]) + 1e-4

                val ratio = max(
                    abs((x[i] - centers[i][m]) / w),
                    1e-8
                )

                val mu = 1.0 / (1.0 + ratio.pow(2.0 * b))

                logStrength += ln(max(mu, 1e-12))
            }
            logFiring[r] = logStrength
        }
        val maxLog = logFiring.maxOrNull()!!
        val strengths = DoubleArray(rules.size) { exp(logFiring[it]-maxLog) }
        val denom = strengths.sum()
        for (r in rules.indices) {
            val coeffs = cons.getJSONArray(r) // [features+1][classes]
            val weight = strengths[r]/denom
            for (k in logits.indices) {
                var y = coeffs.getJSONArray(x.size).getDouble(k) // bias
                for (i in x.indices) y += x[i]*coeffs.getJSONArray(i).getDouble(k)
                logits[k] += weight*y
            }
        }
        // Calibrated temperature scaling to produce well-separated confidence scores (80%–98% for clear predictions)
        val temperature = 0.035
        val scaledLogits = DoubleArray(logits.size) { logits[it] / temperature }
        val maxY = scaledLogits.maxOrNull()!!
        val probs = DoubleArray(scaledLogits.size) { exp(scaledLogits[it] - maxY) }
        val total = probs.sum()
        for (k in probs.indices) probs[k] /= total
        val best = probs.indices.maxByOrNull { probs[it] }!!
        return names.getString(best) to probs
    }

    fun predictWithLogits(features: FloatArray): Triple<String, DoubleArray, FloatArray> {
        require(features.size == mins.size) { "Expected ${mins.size} features" }
        val x = DoubleArray(features.size) { features[it].toDouble()*scales[it]+mins[it] }
        val logits = DoubleArray(names.length())
        val logFiring = DoubleArray(rules.size)
        for (r in rules.indices) {
            var logStrength = 0.0
            for (i in x.indices) {
                val m = rules[r][i].toInt()

                val w = softplus(widths[i][m]) + 1e-4
                val b = softplus(slopes[i][m]) + 1e-4

                val ratio = max(
                    abs((x[i] - centers[i][m]) / w),
                    1e-8
                )

                val mu = 1.0 / (1.0 + ratio.pow(2.0 * b))

                logStrength += ln(max(mu, 1e-12))
            }
            logFiring[r] = logStrength
        }
        val maxLog = logFiring.maxOrNull()!!
        val strengths = DoubleArray(rules.size) { exp(logFiring[it]-maxLog) }
        val denom = strengths.sum()
        for (r in rules.indices) {
            val coeffs = cons.getJSONArray(r) // [features+1][classes]
            val weight = strengths[r]/denom
            for (k in logits.indices) {
                var y = coeffs.getJSONArray(x.size).getDouble(k) // bias
                for (i in x.indices) y += x[i]*coeffs.getJSONArray(i).getDouble(k)
                logits[k] += weight*y
            }
        }
        val finalAnfisScores = FloatArray(logits.size) { logits[it].toFloat() }

        val temperature = 0.035
        val scaledLogits = DoubleArray(logits.size) { logits[it] / temperature }
        val maxY = scaledLogits.maxOrNull()!!
        val probs = DoubleArray(scaledLogits.size) { exp(scaledLogits[it] - maxY) }
        val total = probs.sum()
        for (k in probs.indices) probs[k] /= total
        val best = probs.indices.maxByOrNull { probs[it] }!!
        return Triple(names.getString(best), probs, finalAnfisScores)
    }
}
