package com.jae464.placememo.data.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.jae464.placememo.domain.model.post.Memo
import java.security.Timestamp
import java.util.*

@Entity(tableName = "memo")
data class MemoEntity(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: Date,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @Embedded
    val region: Region? = null,
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true) val id: Long=0,
)

data class Region(
    val area1: String = "",
    val area2: String = "",
    val area3: String = "",
)