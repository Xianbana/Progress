package com.xian.progress.demo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.xian.progress.demo.databinding.ActivityMainBinding

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
        // 测试HorizontalProgressBar - 简单直接
        itemBar1.setEnableAnimation(true)
        itemBar1.setAnimationDuration(2000L) // 2秒动画
        itemBar1.setProgress(75)
        
        // 测试CircularProgressBar - 简单直接
        kpiCircle1.setEnableAnimation(true)
        kpiCircle1.setAnimationDuration(3000L) // 3秒动画
        kpiCircle1.setProgress(80)
    }
}