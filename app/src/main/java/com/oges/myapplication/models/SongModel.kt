package com.oges.myapplication.models

data class SongModel(
    val id: Int,
    val title: String,
    val author: String,
    val audioUrl: String,
    val coverImage: String
)