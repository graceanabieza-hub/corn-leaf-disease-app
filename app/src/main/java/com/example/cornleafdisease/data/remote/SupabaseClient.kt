package com.example.cornleafdisease.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    // Project URL constructed from your Project ID: rftutgvcfvnneolvglj
    const val SUPABASE_URL = "https://rftutgvcfvnneolvglj.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJmdHV0Z3ZjcWZ2bm5lb2x2Z2xqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwODA4MzgsImV4cCI6MjEwNTY1NjgzOH0.TjrcRw1YpnNbgXcOthLpkd6cAJJZAMbAXLsE6jAhyl0"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }
}
