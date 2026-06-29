package com.ohshootstudio.resibooth.domain

import org.json.JSONArray
import org.json.JSONObject

data class CustomFrame(
    val id: String,
    val x: Float, // Normalized 0.0 to 1.0
    val y: Float, // Normalized 0.0 to 1.0
    val width: Float, // Normalized 0.0 to 1.0
    val height: Float // Normalized 0.0 to 1.0
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("x", x.toDouble())
        obj.put("y", y.toDouble())
        obj.put("width", width.toDouble())
        obj.put("height", height.toDouble())
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): CustomFrame {
            return CustomFrame(
                id = obj.getString("id"),
                x = obj.getDouble("x").toFloat(),
                y = obj.getDouble("y").toFloat(),
                width = obj.getDouble("width").toFloat(),
                height = obj.getDouble("height").toFloat()
            )
        }
    }
}

data class CustomTemplate(
    val frames: List<CustomFrame> = emptyList(),
    val aspectRatio: Float = 1.5f // Default 1.5 (e.g. 600x900)
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        val framesArray = JSONArray()
        frames.forEach { framesArray.put(it.toJson()) }
        obj.put("frames", framesArray)
        obj.put("aspectRatio", aspectRatio.toDouble())
        return obj.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String?): CustomTemplate {
            if (jsonStr.isNullOrBlank()) return CustomTemplate()
            return try {
                val obj = JSONObject(jsonStr)
                val framesArray = obj.getJSONArray("frames")
                val frames = mutableListOf<CustomFrame>()
                for (i in 0 until framesArray.length()) {
                    frames.add(CustomFrame.fromJson(framesArray.getJSONObject(i)))
                }
                val aspectRatio = if (obj.has("aspectRatio")) obj.getDouble("aspectRatio").toFloat() else 1.5f
                CustomTemplate(frames, aspectRatio)
            } catch (e: Exception) {
                CustomTemplate()
            }
        }
    }
}

