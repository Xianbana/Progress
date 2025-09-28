package com.xian.progress.demo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.xian.progress.demo.databinding.ActivityMainBinding
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {
    private lateinit var bin: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bin = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bin.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDashboard()
        setupRandomize()


        bin.test.setOnClickListener {
            // 为水平进度条设置单个属性，测试配置是否自动应用
            bin.itemBar1.setBackgroundColor("#FFF3E0".toColorInt()) // 浅橙色背景
            bin.itemBar1.setProgressColor("#FF9800".toColorInt())   // 橙色进度
            bin.itemBar1.setEnableAnimation(true)
            bin.itemBar1.setAnimateFromZero(true)
            bin.itemBar1.setAnimationDuration(1000L) // 3秒动画
            bin.itemBar1.setCornerRadius(360f)         // 圆角半径
        }

    }

    private fun setupDashboard() = with(bin) {
        // KPI 圆环示例
        kpiCircle1.setProgress(72)
        kpiCircle2.setProgress(45)
        kpiCircle3.setProgress(88)

        // 列表水平进度条及文案
        itemBar1.setProgress(80)
        itemValue1.text = "80%"

        itemBar2.setProgress(55)
        itemValue2.text = "55%"

        itemBar3.setProgress(30)
        itemValue3.text = "30%"

        itemBar4.setProgress(95)
        itemValue4.text = "95%"
    }

    private fun setupRandomize() = with(bin) {
        btnRandomize.setOnClickListener {
            fun rand() = listOf(15, 25, 35, 45, 55, 65, 75, 85, 95).random()

            val a = rand(); val b = rand(); val c = rand();
            kpiCircle1.setProgress(a)
            kpiCircle2.setProgress(b)
            kpiCircle3.setProgress(c)

            val m1 = rand(); val m2 = rand(); val m3 = rand(); val m4 = rand()
            itemBar1.setProgress(m1); itemValue1.text = "$m1%"
            itemBar2.setProgress(m2); itemValue2.text = "$m2%"
            itemBar3.setProgress(m3); itemValue3.text = "$m3%"
            itemBar4.setProgress(m4); itemValue4.text = "$m4%"
            
            // 测试新添加的setter/getter方法
            testNewSetters()
        }
    }
    
    private fun testNewSetters() = with(bin) {
        // 测试HorizontalProgressBar的setProgressConfig方法
        val config = com.xian.progress.model.ProgressConfig(
            backgroundColor = "#E8F5E9".toColorInt(), // 浅绿色背景
            progressColor = "#4CAF50".toColorInt(),   // 绿色进度
            enableAnimation = true,
            animateFromZero = true,
            animationDuration = 2500L, // 2.5秒动画
            cornerRadius = 20f         // 圆角半径
        )
        itemBar1.setProgressConfig(config)
        // 不需要再调用setProgress，配置会自动应用

        // 测试CircularProgressBar - 简单直接
        kpiCircle1.setEnableAnimation(true)
        kpiCircle1.setAnimationDuration(3000L) // 3秒动画
        kpiCircle1.setProgress(80)
    }
}