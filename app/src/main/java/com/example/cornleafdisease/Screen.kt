package com.example.cornleafdisease

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DetectDisease : Screen("detect_disease")
    object PredictionResult : Screen("prediction_result")
    object DiseaseInfo : Screen("disease_info")
    object PredictionHistory : Screen("prediction_history")
}
