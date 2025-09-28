package com.xian.progress.demo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
            // 随机选择3、4、5秒作为动画时间
            val animationDuration = listOf(7000L).random()
            
            // 设置item_bar_5的动画时间
            bin.itemBar5.setAnimationDuration(animationDuration)
            bin.itemBar5.setEnableAnimation(true)
            bin.itemBar5.setAnimateFromZero(true)
            bin.itemBar5.setProgress(100)
            
            // 实现time TextView从0-100的动画效果
            animateTimeTextView(animationDuration)
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
    
    private fun animateTimeTextView(duration: Long) {
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())
        val interval = 16L // 60fps
        
        val runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - startTime
                
                if (elapsed < duration) {
                    // 基于实际经过的时间计算进度，更精确
                    val progress = (elapsed * 100f / duration).toInt().coerceAtMost(100)
                    bin.time.text = "$progress%"
                    handler.postDelayed(this, interval)
                } else {
                    // 确保最后显示100%
                    bin.time.text = "100%"
                    val actualDuration = currentTime - startTime
                    Log.d("xianban", "设置时间: ${duration}ms, 实际耗时: ${actualDuration}ms, 误差: ${actualDuration - duration}ms")
                }
            }
        }
        
        // 重置为0开始动画
        bin.time.text = "0%"
        handler.post(runnable)
    }
}