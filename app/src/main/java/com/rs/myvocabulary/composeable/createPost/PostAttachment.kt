package com.rs.learnmedia.composeable.createPost

import android.net.Uri
import java.io.File

data class PostAttachment(
        val uri: Uri? = null,
        val type: String,
        val name: String?,
        val file: File? = null,
        val remoteUrl: String? = null
)
