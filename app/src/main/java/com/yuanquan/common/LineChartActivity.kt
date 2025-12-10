package com.yuanquan.common

import android.os.Handler
import android.os.Looper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yuanquan.common.ui.base.BaseActivity
import com.yuanquan.common.ui.base.BaseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import com.yuanquan.common.databinding.ActivityLineChartBinding

class LineChartActivity :
    BaseActivity<BaseViewModel<ActivityLineChartBinding>, ActivityLineChartBinding>() {

    // 图表相关变量
    private var isUpdating = false
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateCount = 0
    private val dataPointsLimit = 10 // 最多显示的数据点数

    // 三条折线的数据
    private val greenEntries = mutableListOf<Entry>()
    private val blueEntries = mutableListOf<Entry>()
    private val redEntries = mutableListOf<Entry>()

    // 时间格式
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var startTime: Long = 0

    // 数据生成器
    private var greenBaseValue = 100f
    private var blueBaseValue = 100f
    private var redBaseValue = 50f

    // 更新间隔（毫秒）
    private val updateInterval = 1000L

    // 颜色定义
    private val greenColor = android.graphics.Color.parseColor("#4CAF50")
    private val blueColor = android.graphics.Color.parseColor("#2196F3")
    private val redColor = android.graphics.Color.parseColor("#F44336")

    override fun initView() {
        // 初始化图表
        setupChart()

        // 设置按钮点击事件
        setupButtons()

        // 初始化时间
        startTime = System.currentTimeMillis()
        updateTimeDisplay()
    }

    override fun initData() {

    }

    private fun setupChart() {
        // 1. 基本配置
        vb.lineChart.setTouchEnabled(true)
        vb.lineChart.setPinchZoom(true)
        vb.lineChart.description.isEnabled = false

        // 2. 配置X轴
        val xAxis = vb.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = 5
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val date = Date(startTime + value.toLong() * 60000) // 每单位代表1分钟
                return timeFormat.format(date)
            }
        }

        // 3. 配置Y轴（左侧）
        val leftAxis = vb.lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 250f
        leftAxis.granularity = 50f
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawZeroLine(false)

        // 4. 配置Y轴（右侧）- 禁用
        val rightAxis = vb.lineChart.axisRight
        rightAxis.isEnabled = false

        // 5. 配置图例
        val legend = vb.lineChart.legend
        legend.isEnabled = true
        legend.verticalAlignment =
            com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment =
            com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation =
            com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(true)

        // 6. 初始化三条折线
        initializeDataSets()
    }

    private fun initializeDataSets() {
        // 创建三条折线的数据集
        val greenDataSet = createDataSet(greenEntries, "绿色", greenColor)
        val blueDataSet = createDataSet(blueEntries, "蓝色", blueColor)
        val redDataSet = createDataSet(redEntries, "红色", redColor)

        // 创建折线数据
        val lineData = LineData(greenDataSet, blueDataSet, redDataSet)
        lineData.setValueTextSize(10f)
        lineData.setValueTextColor(android.graphics.Color.DKGRAY)

        // 设置到图表
        vb.lineChart.data = lineData
        vb.lineChart.invalidate()
    }

    private fun createDataSet(entries: List<Entry>, label: String, color: Int): LineDataSet {
        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.2f

        // 启用填充
        dataSet.setDrawFilled(true)
        dataSet.fillColor = color
        dataSet.fillAlpha = 20

        return dataSet
    }

    private fun setupButtons() {
        vb.btnStart.setOnClickListener {
            startDataUpdate()
        }

        vb.btnPause.setOnClickListener {
            stopDataUpdate()
        }

        vb.btnReset.setOnClickListener {
            resetData()
        }
    }

    private fun startDataUpdate() {
        if (!isUpdating) {
            isUpdating = true
            updateData()
        }
    }

    private fun stopDataUpdate() {
        isUpdating = false
        updateHandler.removeCallbacksAndMessages(null)
    }

    private fun resetData() {
        stopDataUpdate()

        // 清空数据
        greenEntries.clear()
        blueEntries.clear()
        redEntries.clear()

        // 重置基数
        greenBaseValue = 100f
        blueBaseValue = 100f
        redBaseValue = 50f

        // 重置计数器
        updateCount = 0

        // 更新显示
        updateTimeDisplay()
        updateStatsDisplay()

        // 重置图表
        initializeDataSets()
    }

    private fun updateData() {
        if (!isUpdating) return

        // 生成新数据
        generateNewData()

        // 更新图表
        updateChart()

        // 更新统计信息
        updateStatsDisplay()

        // 更新时间显示
        updateTimeDisplay()

        // 安排下一次更新
        updateHandler.postDelayed({
            updateData()
        }, updateInterval)
    }

    private fun generateNewData() {
        // 限制数据点数量
        if (greenEntries.size >= dataPointsLimit) {
            greenEntries.removeAt(0)
            blueEntries.removeAt(0)
            redEntries.removeAt(0)

            // 调整X轴值
            for (i in greenEntries.indices) {
                greenEntries[i].x = i.toFloat()
                blueEntries[i].x = i.toFloat()
                redEntries[i].x = i.toFloat()
            }
        }

        val xValue = greenEntries.size.toFloat()

        // 生成绿色折线数据（模拟波动）
        greenBaseValue += (Random.nextFloat() - 0.5f) * 20
        greenBaseValue = greenBaseValue.coerceIn(80f, 220f)
        greenEntries.add(Entry(xValue, greenBaseValue))

        // 生成蓝色折线数据
        blueBaseValue += (Random.nextFloat() - 0.5f) * 15
        blueBaseValue = blueBaseValue.coerceIn(70f, 190f)
        blueEntries.add(Entry(xValue, blueBaseValue))

        // 生成红色折线数据
        redBaseValue += (Random.nextFloat() - 0.5f) * 18
        redBaseValue = redBaseValue.coerceIn(40f, 200f)
        redEntries.add(Entry(xValue, redBaseValue))

        updateCount++
    }

    private fun updateChart() {
        // 获取当前数据
        val data = vb.lineChart.data
        if (data != null) {
            // 更新数据集
//            data.dataSets[0].values = greenEntries
//            data.dataSets[1].values = blueEntries
//            data.dataSets[2].values = redEntries
            (data.dataSets[0] as LineDataSet).setValues(greenEntries)

            (data.dataSets[1] as LineDataSet).setValues(blueEntries)

            (data.dataSets[2] as LineDataSet).setValues(redEntries)
            // 动态调整Y轴范围
            adjustYAxisRange()

            // 通知数据已更改
            data.notifyDataChanged()
            vb.lineChart.notifyDataSetChanged()

            // 自动滚动到最新数据
            vb.lineChart.moveViewToX(greenEntries.last().x)

            // 重绘图表
            vb.lineChart.invalidate()
        }
    }

    private fun adjustYAxisRange() {
        // 查找所有数据中的最大值和最小值
        var maxValue = Float.MIN_VALUE
        var minValue = Float.MAX_VALUE

        for (entry in greenEntries) {
            maxValue = maxOf(maxValue, entry.y)
            minValue = minOf(minValue, entry.y)
        }
        for (entry in blueEntries) {
            maxValue = maxOf(maxValue, entry.y)
            minValue = minOf(minValue, entry.y)
        }
        for (entry in redEntries) {
            maxValue = maxOf(maxValue, entry.y)
            minValue = minOf(minValue, entry.y)
        }

        // 添加一些边距
        val margin = 20f
        val leftAxis = vb.lineChart.axisLeft
        leftAxis.axisMinimum = (minValue - margin).coerceAtLeast(0f)
        leftAxis.axisMaximum = maxValue + margin

        // 动态调整X轴标签数量
        val xAxis = vb.lineChart.xAxis
        xAxis.labelCount = minOf(greenEntries.size, 8)
    }

    private fun updateStatsDisplay() {
        // 更新统计信息
        if (greenEntries.isNotEmpty()) {
            val greenValue = greenEntries.last().y
            val blueValue = blueEntries.last().y
            val redValue = redEntries.last().y

            vb.tvGreenStats.text = String.format("绿色: %.1f", greenValue)
            vb.tvBlueStats.text = String.format("蓝色: %.1f", blueValue)
            vb.tvRedStats.text = String.format("红色: %.1f", redValue)
        }

        // 更新数据点数
        vb.tvDataCount.text = "数据点数: ${greenEntries.size}"
    }

    private fun updateTimeDisplay() {
        val currentTime = Date(startTime + updateCount * 60000L)
        vb.tvCurrentTime.text = "当前时间: ${timeFormat.format(currentTime)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDataUpdate()
    }
}