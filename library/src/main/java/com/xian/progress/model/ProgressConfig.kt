package com.xian.progress.model

import androidx.annotation.ColorInt

/**
 * 进度条配置类
 * @param backgroundColor 背景颜色
 * @param progressColor 进度颜色
 * @param enableAnimation 是否启用动画
 * @param animateFromZero 动画是否从0开始
 * @param animationDuration 动画时长（毫秒）
 * @param cornerRadius 圆角半径
 */
data class ProgressConfig(
    @ColorInt val backgroundColor: Int = 0xFFF3F3F3.toInt(),
    @ColorInt val progressColor: Int = 0xFFFF5722.toInt(), // 橙色
    val enableAnimation: Boolean = true,
    val animateFromZero: Boolean = true,
    val animationDuration: Long = 1000L,
    val cornerRadius: Float = 360f
)
