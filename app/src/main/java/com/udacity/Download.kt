package com.udacity

import androidx.annotation.StringRes

enum class Download(val url: String, @StringRes val text: Int) {
    UDACITY(
        "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
        R.string.udacity
    ),
    GLIDE(
        "https://github.com/bumptech/glide/archive/master.zip",
        R.string.glide
    ),
    RETROFIT(
        "https://github.com/square/retrofit/archive/master.zip",
        R.string.retrofit
    );
}