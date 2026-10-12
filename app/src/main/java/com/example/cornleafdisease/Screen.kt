package com.example.cornleafdisease

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DetectDisease : Screen("detect_disease")
    object ClassificationResult : Screen("classification_result")
    object DiseaseInfo : Screen("disease_info")
    object ClassificationHistory : Screen("classification_history")
}
