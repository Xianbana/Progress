package com.xian.progress.model

import androidx.annotation.ColorInt

/**
 * 进度条数据类
 * @param progress 进度值 (0-100)
 * @param color 进度颜色，如果为null则使用配置中的默认颜色
 */
data class ProgressData(
    val progress: Number,
    @ColorInt val color: Int? = null
) {
    val progressValue: Float
        get() = when (progress) {
            is Int -> progress.toFloat()
            is Float -> progress
            is Double -> progress.toFloat()
            is Long -> progress.toFloat()
            is Short -> progress.toFloat()
            is Byte -> progress.toFloat()
            else -> progress.toFloat()
        }
    
    init {
        val progressFloat = progressValue
        require(progressFloat in 0f..100f) { 
            "Progress must be between 0 and 100, got: $progressFloat" 
        }
    }
    
    companion object {
        /**
         * 创建进度数据
         */
        @JvmStatic
        fun create(progress: Number, color: Int? = null): ProgressData {
            return ProgressData(progress, color)
        }
        
        /**
         * 创建int进度数据
         */
        @JvmStatic
        fun createInt(progress: Int, color: Int? = null): ProgressData {
            return ProgressData(progress, color)
        }
        
        /**
         * 创建float进度数据
         */
        @JvmStatic
        fun createFloat(progress: Float, color: Int? = null): ProgressData {
            return ProgressData(progress, color)
        }
    }
}
