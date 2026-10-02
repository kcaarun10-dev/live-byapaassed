package com.example.extractor

data class ExtractedStream(
    val url: String,
    val headers: Map<String, String>,
    val cookies: String?
)
