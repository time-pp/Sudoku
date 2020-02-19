package com.timepp.sudoku.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.timepp.sudoku.R
import com.timepp.sudoku.data.AbsSudoku.Companion.SUDO_UNIT
import com.timepp.sudoku.data.Sudoku
import com.timepp.sudoku.data.SudokuItem
import com.timepp.sudoku.data.SudokuItem.Companion.NOT_SURE_NUM
import kotlin.math.min

class SudokuView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val FONT_SCALE = 2 / 3f
        private const val NOTE_FONT_SCALE = 5 / 6f
        private const val BOX_UNIT = 3
        private const val BOX_LINE_COUNT = 4
        private const val GRID_LINE_COUNT = 6
    }
    var sudoku: Sudoku? = null
    set(value) {
        field = value
        invalidate()
    }
    private var selectPosition = -1
    private val drawHelper = DrawHelper()
    private val paint = Paint()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val touchDownPoint = PointF()
    init {
        textPaint.textAlign = Paint.Align.CENTER
        setOnClickListener { onClick() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(getMeasureSize(widthMeasureSpec), getMeasureSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
        drawHelper.viewSize = size
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }
        drawBackground(canvas)
        drawNumbers(canvas)
        drawGrid(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            touchDownPoint.set(event.x, event.y)
        }
        return super.onTouchEvent(event)
    }

    private fun drawNumbers(canvas: Canvas) {
        val sudoku = this.sudoku ?: return
        drawHelper.apply {
            textPaint.textSize = fontSize
            var startY = -gridSize
            for (y in 0 until SUDO_UNIT) {
                startY += gridSize + if (y % BOX_UNIT == 0) boxLineWidth else gridLineWidth
                drawLineNumbers(canvas, sudoku, y, startY)
            }
        }
    }

    private fun drawLineNumbers(canvas: Canvas, sudoku: Sudoku, y: Int, startY: Float) {
        drawHelper.apply {
            paint.style = Paint.Style.FILL
            var startX = -gridSize
            var sudokuItem: SudokuItem
            val startIndex = y * SUDO_UNIT
            val selectValue = if (selectPosition >= 0) sudoku.sudokuItems[selectPosition].value else -1
            var sudokuIndex: Int
            for (x in 0 until SUDO_UNIT) {
                startX += gridSize + if (x % BOX_UNIT == 0) boxLineWidth else gridLineWidth
                sudokuIndex = startIndex + x
                sudokuItem = sudoku.sudokuItems[sudokuIndex]
                if (sudokuIndex == selectPosition) {
                    paint.color = selectNumberBackColor
                    canvas.drawRect(startX, startY, startX + gridSize, startY + gridSize, paint)
                } else if (sudokuItem.value == selectValue) {
                    paint.color = selectNumbersBackColor
                    canvas.drawRect(startX, startY, startX + gridSize, startY + gridSize, paint)
                }
                if (sudokuItem.value != NOT_SURE_NUM) {
                    textPaint.color = if (sudokuItem.isFixed) globalColor else fillNumberColor
                    canvas.drawText(sudokuItem.value.toString(), startX + textX, startY + textY, textPaint)
                } else {
                    drawNote(canvas, startX, startY, sudokuItem.noteArray)
                }
            }
        }
    }

    private fun drawNote(canvas: Canvas, startX: Float, startY: Float, noteArray: BooleanArray?) {
        if (noteArray == null) {
            return
        }
        drawHelper.apply {
            textPaint.textSize = noteFontSize
            textPaint.color = noteColor
            var noteStartX = startX
            var noteStartY = startY
            for ((index, isMark) in noteArray.withIndex()) {
                if (isMark) {
                    canvas.drawText((index + 1).toString(), noteStartX + noteTextX, noteStartY + noteTextY, textPaint)
                }
                if (index % BOX_UNIT == BOX_UNIT - 1) {
                    noteStartX = startX
                    noteStartY += noteSize
                } else {
                    noteStartX += noteSize
                }
            }
            textPaint.textSize = fontSize
        }
    }

    private fun drawGrid(canvas: Canvas) {
        paint.color = drawHelper.gridColor
        paint.strokeWidth = drawHelper.gridLineWidth
        paint.style = Paint.Style.STROKE
        canvas.drawPath(drawHelper.unitGridPath, paint)
        paint.color = drawHelper.globalColor
        paint.strokeWidth = drawHelper.boxLineWidth
        canvas.drawPath(drawHelper.boxPath, paint)
    }

    private fun drawBackground(canvas: Canvas) {
        paint.color = drawHelper.selectBackColor
        paint.style = Paint.Style.FILL
        canvas.drawPath(drawHelper.selectAreaPath, paint)
    }

    private fun getMeasureSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return when(MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> size
            else -> resources.getDimensionPixelOffset(R.dimen.default_sudoku_size)
        }
    }

    private fun onClick() {
        val row = getItemPosition(touchDownPoint.y)
        val column = getItemPosition(touchDownPoint.x)
        if (row or column < 0) {
            return
        }
        selectPosition = row * SUDO_UNIT + column
        drawHelper.initSelectAreaPath()
        invalidate()
    }


    private fun getItemPosition(coordinate: Float): Int {
        drawHelper.apply {
            var x = coordinate
            for (i in 0 until SUDO_UNIT) {
                x -= if (i % BOX_UNIT == 0) boxLineWidth else gridLineWidth
                if (x <= 0) {
                    return -1
                }
                x -= gridSize
                if (x < 0) {
                    return i
                }
            }
        }
        return -1
    }
    inner class DrawHelper {
        // 所有大宫格的分隔线组成的path
        val boxPath = Path()
        // 所有小格子的分隔线组成的path
        val unitGridPath = Path()
        val selectAreaPath = Path()
        // 小格子的大小
        var gridSize = 0f
            private set
        // 笔记格子的大小
        var noteSize = 0f
            private set
        // 数字的字体大小
        var fontSize = 0f
            private set
        // 笔记的字体大小
        var noteFontSize = 0f
            private set
        // 数字相对于小格子顶部的位置
        var textY = 0f
            private set
        // 数字相对于小格子左边界的位置
        var textX = 0f
            private set
        // 笔记相对于笔记格子顶部的位置
        var noteTextY = 0f
            private set
        // 笔记相对于笔记格子左边界的位置
        var noteTextX = 0f
            private set
        // 宫格分隔线的宽度
        val boxLineWidth = resources.getDimension(R.dimen.line_width_dp_3)
        // 格子分隔线的宽度
        val gridLineWidth = resources.getDimension(R.dimen.line_width_px_1)
        // 全局颜色
        val globalColor = resources.getColor(R.color.sudoku_global_color)
        // 格子分隔线的颜色
        val gridColor = resources.getColor(R.color.sudoku_grid_color)
        // 选中的区域背景色，选中区域包括选中的行，列以及宫格
        val selectBackColor = resources.getColor(R.color.sudoku_select_area_back_color)
        // 选中的所有数字的背景色，即选中9后，其他位置的9的背景色
        val selectNumbersBackColor = resources.getColor(R.color.sudoku_select_numbers_back_color)
        // 选中的数字的背景色
        val selectNumberBackColor = resources.getColor(R.color.sudoku_select_number_back_color)
        // 笔记文字颜色
        val noteColor = resources.getColor(R.color.sudoku_note_color)
        // 填充的数字的文字颜色
        val fillNumberColor = resources.getColor(R.color.sudoku_fill_number_color)
        var viewSize = 0
        set(value) {
            field = value
            gridSize = (viewSize - boxLineWidth * BOX_LINE_COUNT - gridLineWidth * GRID_LINE_COUNT) / SUDO_UNIT
            noteSize = gridSize / BOX_UNIT
            fontSize = gridSize * FONT_SCALE
            noteFontSize = noteSize * NOTE_FONT_SCALE
            initBoxPath()
            initGridPath()
            initTextPosition()
        }

        private fun initTextPosition() {
            textPaint.textSize = fontSize
            textX = gridSize / 2f
            textY = (gridSize - textPaint.descent() - textPaint.ascent()) / 2
            textPaint.textSize = noteFontSize
            noteTextX = noteSize / 2f
            noteTextY = (noteSize - textPaint.descent() - textPaint.ascent()) / 2
        }

        private fun initBoxPath() {
            val boxSize = (viewSize - boxLineWidth * BOX_LINE_COUNT) / BOX_UNIT
            boxPath.reset()
            val halfLineWidth = boxLineWidth / 2
            var position = halfLineWidth
            val viewSize = viewSize.toFloat()
            for (i in 0..BOX_UNIT) {
                boxPath.moveTo(0f, position)
                boxPath.lineTo(viewSize, position)
                boxPath.moveTo(position, 0f)
                boxPath.lineTo(position, viewSize)
                position += boxSize + boxLineWidth
            }
        }

        private fun initGridPath() {
            unitGridPath.reset()
            var position = 0f
            for (i in 0 until SUDO_UNIT) {
                if (i % BOX_UNIT == 0) {
                    position += boxLineWidth
                    position += gridSize
                    continue
                }
                unitGridPath.moveTo(boxLineWidth, position)
                unitGridPath.lineTo(viewSize - boxLineWidth, position)
                unitGridPath.moveTo(position, boxLineWidth)
                unitGridPath.lineTo(position, viewSize - boxLineWidth)
                position += gridSize + gridLineWidth
            }
        }

        fun initSelectAreaPath() {
            selectAreaPath.reset()
            val row = selectPosition / SUDO_UNIT
            val column = selectPosition % SUDO_UNIT
            val selectGridLeft = getCoordinate(column)
            val selectGridTop = getCoordinate(row)
            val selectBoxLeft = getCoordinate(column / BOX_UNIT * BOX_UNIT)
            val selectBoxTop = getCoordinate(row / BOX_UNIT * BOX_UNIT)
            selectAreaPath.moveTo(boxLineWidth, selectGridTop)
            selectAreaPath.lineTo(selectBoxLeft, selectGridTop)
            selectAreaPath.lineTo(selectBoxLeft, selectBoxTop)
            selectAreaPath.lineTo(selectGridLeft, selectBoxTop)
            selectAreaPath.lineTo(selectGridLeft, boxLineWidth)
            selectAreaPath.lineTo(selectGridLeft + gridSize, boxLineWidth)
            selectAreaPath.lineTo(selectGridLeft + gridSize, selectBoxTop)
            selectAreaPath.lineTo(selectBoxLeft + BOX_UNIT * gridSize, selectBoxTop)
            selectAreaPath.lineTo(selectBoxLeft + BOX_UNIT * gridSize, selectGridTop)
            selectAreaPath.lineTo(viewSize - boxLineWidth, selectGridTop)
            selectAreaPath.lineTo(viewSize - boxLineWidth, selectGridTop + gridSize)
            selectAreaPath.lineTo(selectBoxLeft + BOX_UNIT * gridSize, selectGridTop + gridSize)
            selectAreaPath.lineTo(selectBoxLeft + BOX_UNIT * gridSize, selectBoxTop + BOX_UNIT * gridSize)
            selectAreaPath.lineTo(selectGridLeft + gridSize, selectBoxTop + BOX_UNIT * gridSize)
            selectAreaPath.lineTo(selectGridLeft + gridSize, viewSize - boxLineWidth)
            selectAreaPath.lineTo(selectGridLeft, viewSize - boxLineWidth)
            selectAreaPath.lineTo(selectGridLeft, selectBoxTop + BOX_UNIT * gridSize)
            selectAreaPath.lineTo(selectBoxLeft, selectBoxTop + BOX_UNIT * gridSize)
            selectAreaPath.lineTo(selectBoxLeft, selectGridTop + gridSize)
            selectAreaPath.lineTo(boxLineWidth, selectGridTop + gridSize)
            selectAreaPath.close()
        }

        private fun getCoordinate(position: Int) = position * gridSize + position / BOX_UNIT * (boxLineWidth + 2 * gridLineWidth) +
                (position % BOX_UNIT) * gridLineWidth + boxLineWidth
    }
}