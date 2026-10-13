package com.sevtinge.hyperceiler.hook.module.hook.home.search

import android.content.Intent
import android.graphics.drawable.Drawable

enum class ResultType { APP, SETTING, WEB, SHORTCUT, MEDIA, FILE, CONTACT, CALENDAR }

data class SearchResult(
    val type: ResultType,
    val label: String,
    val subtitle: String?,
    val icon: Drawable?,
    val intent: Intent,
    val score: Int = 0,
)
