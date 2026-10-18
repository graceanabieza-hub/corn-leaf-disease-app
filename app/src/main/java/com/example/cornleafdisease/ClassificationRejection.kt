package com.example.cornleafdisease


import kotlin.math.exp

data class CheckedClassification(
    val accepted: Boolean,
    val disease: String?,
    val confidence: Float,
    val message: String
)

object ClassificationRejection {

    // Initial value only; validate using real images
    private const val CONFIDENCE_THRESHOLD = 0.70f

    private val classNames = listOf(
        "Common Rust",
        "Healthy Leaves",
        "Northern Leaf Blight",
        "Southern Leaf Blight"
    )

    fun check(finalScores: FloatArray): CheckedClassification {

        require(finalScores.size == 4) {
            "Expected 4 final ANFIS class scores"
        }

        if (finalScores.any { !it.isFinite() }) {
            return CheckedClassification(
                false, null, 0f,
                "Image Not Recognized."
            )
        }

        // Stable softmax for raw logits
        val maxScore = finalScores.maxOrNull()!!
        val exps = finalScores.map {
            exp((it - maxScore).toDouble())
        }
        val sum = exps.sum()

        val probabilities = exps.map {
            (it / sum).toFloat()
        }

        val index = probabilities.indices.maxByOrNull {
            probabilities[it]
        }!!

        val confidence = probabilities[index]

        if (confidence < CONFIDENCE_THRESHOLD) {
            return CheckedClassification(
                accepted = false,
                disease = null,
                confidence = confidence,
                message = "Image Not Recognized. Please upload a clear corn leaf image."
            )
        }

        return CheckedClassification(
            accepted = true,
            disease = classNames[index],
            confidence = confidence,
            message = "Classification successful."
        )
    }
}
