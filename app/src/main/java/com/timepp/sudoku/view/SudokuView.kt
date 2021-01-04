package com.timepp.sudoku.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.timepp.sudoku.R
import com.timepp.sudoku.data.AbsSudoku.Companion.SUDO_UNIT
import com.timepp.sudoku.data.SudokuItem
import com.timepp.sudoku.data.SudokuItem.Companion.NOT_SURE_NUM
import com.timepp.sudoku.data.SudokuRiddle
import com.timepp.sudoku.data.SudokuRiddle.Companion.FILL_RESULT_CAN_NOT_FILL
import kotlin.math.min

class SudokuView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val FONT_SCALE = 2 / 3f
        private const val NOTE_FONT_SCALE = 5 / 6f
        private const val BOX_ROWS = 3
        private const val BOX_DIV_COUNT_IN_SUDOKU = 4
        private const val CELL_DIV_COUNT_IN_SUDOKU = 6
        private const val CELL_DIV_COUNT_IN_BOX = 2
    }
    var sudokuRiddle: SudokuRiddle? = null
    set(value) {
        field = value
        value?.resetErrorCell()
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
        var size = min(getMeasureSize(widthMeasureSpec), getMeasureSize(heightMeasureSpec))
        val lineWidth = drawHelper.boxLineWidth * BOX_DIV_COUNT_IN_SUDOKU + drawHelper.cellLineWidth * CELL_DIV_COUNT_IN_SUDOKU
        size -= (size - lineWidth.toInt()) % SUDO_UNIT
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
        drawCellDiv(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            touchDownPoint.set(event.x, event.y)
        }
        return super.onTouchEvent(event)
    }

    private fun drawNumbers(canvas: Canvas) {
        val sudoku = this.sudokuRiddle ?: return
        drawHelper.apply {
            textPaint.textSize = fontSize
            var startY = -cellSize
            for (y in 0 until SUDO_UNIT) {
                startY += cellSize + if (y % BOX_ROWS == 0) boxLineWidth else cellLineWidth
                drawLineNumbers(canvas, sudoku, y, startY)
            }
        }
    }

    private fun drawLineNumbers(canvas: Canvas, sudokuRiddle: SudokuRiddle, y: Int, startY: Float) {
        drawHelper.apply {
            paint.style = Paint.Style.FILL
            var startX = -cellSize
            var sudokuItem: SudokuItem
            val startIndex = y * SUDO_UNIT
            val selectValue = if (selectPosition >= 0) sudokuRiddle.sudokuItems[selectPosition].value else -1
            var sudokuIndex: Int
            for (x in 0 until SUDO_UNIT) {
                startX += cellSize + if (x % BOX_ROWS == 0) boxLineWidth else cellLineWidth
                sudokuIndex = startIndex + x
                sudokuItem = sudokuRiddle.sudokuItems[sudokuIndex]
                if (sudokuIndex == selectPosition) {
                    paint.color = selectNumberBackColor
                    canvas.drawRect(startX, startY, startX + cellSize, startY + cellSize, paint)
                } else if (sudokuItem.value == selectValue && selectValue != NOT_SURE_NUM) {
                    paint.color = selectNumbersBackColor
                    canvas.drawRect(startX, startY, startX + cellSize, startY + cellSize, paint)
                }
                if (sudokuItem.value != NOT_SURE_NUM) {
                    textPaint.color = when {
                        sudokuItem.isError || sudokuIndex in sudokuRiddle.conflictCellPos -> errorNumberColor
                        sudokuItem.isFixed -> globalColor
                        else -> fillNumberColor
                    }
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
                if (index % BOX_ROWS == BOX_ROWS - 1) {
                    noteStartX = startX
                    noteStartY += noteSize
                } else {
                    noteStartX += noteSize
                }
            }
            textPaint.textSize = fontSize
        }
    }

    private fun drawCellDiv(canvas: Canvas) {
        paint.color = drawHelper.cellDivColor
        paint.strokeWidth = drawHelper.cellLineWidth
        paint.style = Paint.Style.STROKE
        canvas.drawPath(drawHelper.cellDivPath, paint)
        paint.color = drawHelper.globalColor
        paint.strokeWidth = drawHelper.boxLineWidth
        canvas.drawPath(drawHelper.boxDivPath, paint)
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
        sudokuRiddle?.checkSelectCell(selectPosition)
        invalidate()
    }


    private fun getItemPosition(coordinate: Float): Int {
        drawHelper.apply {
            var x = coordinate
            for (i in 0 until SUDO_UNIT) {
                x -= if (i % BOX_ROWS == 0) boxLineWidth else cellLineWidth
                if (x <= 0) {
                    return -1
                }
                x -= cellSize
                if (x < 0) {
                    return i
                }
            }
        }
        return -1
    }

    fun fillNum(number: Int): Int {
        val result = sudokuRiddle?.fillNum(selectPosition, number) ?: FILL_RESULT_CAN_NOT_FILL
        invalidate()
        return result
    }

    inner class DrawHelper {
        // 所有大宫格的分隔线组成的path
        val boxDivPath = Path()
        // 所有小格子的分隔线组成的path
        val cellDivPath = Path()
        val selectAreaPath = Path()
        // 小格子的大小
        var cellSize = 0f
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
        val cellLineWidth = resources.getDimension(R.dimen.line_width_px_1)
        // 全局颜色
        val globalColor = ResourcesCompat.getColor(resources, R.color.sudoku_global_color, null)
        // 格子分隔线的颜色
        val cellDivColor = ResourcesCompat.getColor(resources, R.color.sudoku_cell_div_color, null)
        // 选中的区域背景色，选中区域包括选中的行，列以及宫格
        val selectBackColor = ResourcesCompat.getColor(resources, R.color.sudoku_select_area_back_color, null)
        // 选中的所有数字的背景色，即选中9后，其他位置的9的背景色
        val selectNumbersBackColor = ResourcesCompat.getColor(resources, R.color.sudoku_select_numbers_back_color, null)
        // 选中的数字的背景色
        val selectNumberBackColor = ResourcesCompat.getColor(resources, R.color.sudoku_select_number_back_color, null)
        // 笔记文字颜色
        val noteColor = ResourcesCompat.getColor(resources, R.color.sudoku_note_color, null)
        // 填充的数字的文字颜色
        val fillNumberColor = ResourcesCompat.getColor(resources, R.color.sudoku_fill_number_color, null)
        // 填错的数字的文字颜色
        val errorNumberColor = ResourcesCompat.getColor(resources, R.color.sudoku_error_number_color, null)
        var viewSize = 0
        set(value) {
            field = value
            cellSize = (viewSize - boxLineWidth * BOX_DIV_COUNT_IN_SUDOKU - cellLineWidth * CELL_DIV_COUNT_IN_SUDOKU) / SUDO_UNIT
            noteSize = cellSize / BOX_ROWS
            fontSize = cellSize * FONT_SCALE
            noteFontSize = noteSize * NOTE_FONT_SCALE
            initBoxDivPath()
            initCellDivPath()
            initTextPosition()
        }

        private fun initTextPosition() {
            textPaint.textSize = fontSize
            textX = cellSize / 2f
            textY = (cellSize - textPaint.descent() - textPaint.ascent()) / 2
            textPaint.textSize = noteFontSize
            noteTextX = noteSize / 2f
            noteTextY = (noteSize - textPaint.descent() - textPaint.ascent()) / 2
        }

        private fun initBoxDivPath() {
            val boxSize = (viewSize - boxLineWidth * BOX_DIV_COUNT_IN_SUDOKU) / BOX_ROWS
            boxDivPath.reset()
            val halfLineWidth = boxLineWidth / 2
            var position = halfLineWidth
            val viewSize = viewSize.toFloat()
            for (i in 0..BOX_ROWS) {
                boxDivPath.moveTo(0f, position)
                boxDivPath.lineTo(viewSize, position)
                boxDivPath.moveTo(position, 0f)
                boxDivPath.lineTo(position, viewSize)
                position += boxSize + boxLineWidth
            }
        }

        private fun initCellDivPath() {
            cellDivPath.reset()
            var position = 0f
            for (i in 0 until SUDO_UNIT) {
                if (i % BOX_ROWS == 0) {
                    position += boxLineWidth
                    position += cellSize
                    continue
                }
                cellDivPath.moveTo(boxLineWidth, position)
                cellDivPath.lineTo(viewSize - boxLineWidth, position)
                cellDivPath.moveTo(position, boxLineWidth)
                cellDivPath.lineTo(position, viewSize - boxLineWidth)
                position += cellSize + cellLineWidth
            }
        }

        fun initSelectAreaPath() {
            selectAreaPath.reset()
            val row = selectPosition / SUDO_UNIT
            val column = selectPosition % SUDO_UNIT
            val selectColumnLeft = getCoordinate(column)
            val selectRowTop = getCoordinate(row)
            val selectBoxLeft = getCoordinate(column / BOX_ROWS * BOX_ROWS)
            val selectBoxTop = getCoordinate(row / BOX_ROWS * BOX_ROWS)
            val boxWidth = cellLineWidth * CELL_DIV_COUNT_IN_BOX + BOX_ROWS * cellSize
            selectAreaPath.moveTo(boxLineWidth, selectRowTop)
            selectAreaPath.lineTo(selectBoxLeft, selectRowTop)
            selectAreaPath.lineTo(selectBoxLeft, selectBoxTop)
            selectAreaPath.lineTo(selectColumnLeft, selectBoxTop)
            selectAreaPath.lineTo(selectColumnLeft, boxLineWidth)
            selectAreaPath.lineTo(selectColumnLeft + cellSize, boxLineWidth)
            selectAreaPath.lineTo(selectColumnLeft + cellSize, selectBoxTop)
            selectAreaPath.lineTo(selectBoxLeft + boxWidth, selectBoxTop)
            selectAreaPath.lineTo(selectBoxLeft + boxWidth, selectRowTop)
            selectAreaPath.lineTo(viewSize - boxLineWidth, selectRowTop)
            selectAreaPath.lineTo(viewSize - boxLineWidth, selectRowTop + cellSize)
            selectAreaPath.lineTo(selectBoxLeft + boxWidth, selectRowTop + cellSize)
            selectAreaPath.lineTo(selectBoxLeft + boxWidth, selectBoxTop + boxWidth)
            selectAreaPath.lineTo(selectColumnLeft + cellSize, selectBoxTop + boxWidth)
            selectAreaPath.lineTo(selectColumnLeft + cellSize, viewSize - boxLineWidth)
            selectAreaPath.lineTo(selectColumnLeft, viewSize - boxLineWidth)
            selectAreaPath.lineTo(selectColumnLeft, selectBoxTop + boxWidth)
            selectAreaPath.lineTo(selectBoxLeft, selectBoxTop + boxWidth)
            selectAreaPath.lineTo(selectBoxLeft, selectRowTop + cellSize)
            selectAreaPath.lineTo(boxLineWidth, selectRowTop + cellSize)
            selectAreaPath.close()
        }

        private fun getCoordinate(position: Int) = position * cellSize + position / BOX_ROWS * (boxLineWidth + 2 * cellLineWidth) +
                (position % BOX_ROWS) * cellLineWidth + boxLineWidth
    }
}