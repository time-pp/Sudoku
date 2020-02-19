package com.timepp.sudoku.data

import com.timepp.sudoku.SudokuUtils
import java.util.*
import kotlin.collections.ArrayList

class Sudoku(getValue: (index: Int) -> Int) : AbsSudoku() {
    override val sudokuItems: Array<SudokuItem>
    private val waitToFillItems = ArrayList<SudokuItem>()
    private val fillStack = Stack<FillStep>()
    private val stepCache: ArrayList<FillStep>

    companion object {
        fun buildByValues(sudokuValues: IntArray): Sudoku {
            if (sudokuValues.size != SUDO_SIZE) {
                throw IllegalArgumentException("values size must be $SUDO_SIZE")
            }
            return Sudoku() {pos -> sudokuValues[pos]}
        }

        fun buildByFileContent(sudokuValues: ByteArray): Sudoku {
            if (sudokuValues.size != SudokuUtils.SUDOKU_MIN_SIZE) {
                throw IllegalArgumentException("values size must be ${SudokuUtils.SUDOKU_MIN_SIZE}")
            }
            var currentValueIndex = 0
            var currentValue = sudokuValues[currentValueIndex].toUByte().toInt()
            var valueIndex = currentValue / 10
            return Sudoku() { pos ->
                    if (pos != valueIndex) {
                        SudokuItem.NOT_SURE_NUM
                    } else {
                        val result = currentValue % 10
                        if (++currentValueIndex != SudokuUtils.SUDOKU_MIN_SIZE) {
                            currentValue = sudokuValues[currentValueIndex].toUByte().toInt()
                            valueIndex += currentValue / 10 + 1
                        }
                        result
                    }
            }
        }
    }

    init {
        val sureItems = ArrayList<SudokuItem>()
        sudokuItems = Array(SUDO_SIZE) { i ->
            val value = getValue(i)
            val sudoItem = SudokuItem(value != SudokuItem.NOT_SURE_NUM,
                i % SUDO_UNIT, i / SUDO_UNIT, value)
            if (!sudoItem.isFixed) {
                waitToFillItems.add(sudoItem)
            } else {
                sureItems.add(sudoItem)
            }
            sudoItem
        }
        for (item in sureItems) {
            modifyOptional(item, null)
        }
        stepCache = ArrayList(waitToFillItems.size)
        waitToFillItems.sortBy { it.optionalSet?.size }
    }

    private fun modifyOptional(item: SudokuItem, affectItems: ArrayList<SudokuItem>?) {
        item.apply {
            val startX = x / 3 * 3
            val startY = y / 3 * 3
            var sudokuItem: SudokuItem
            for (i in 0..8) {
                sudokuItem = sudokuItems[i * 9 + x]
                if (sudokuItem.value == SudokuItem.NOT_SURE_NUM && sudokuItem.optionalSet!!.remove(value)) {
                    affectItems?.add(sudokuItem)
                }
                sudokuItem = sudokuItems[y * 9 + i]
                if (sudokuItem.value == SudokuItem.NOT_SURE_NUM && sudokuItem.optionalSet!!.remove(value)) {
                    affectItems?.add(sudokuItem)
                }
                sudokuItem = sudokuItems[(i / 3 + startY) * 9 + (i % 3 + startX)]
                if (sudokuItem.value == SudokuItem.NOT_SURE_NUM && sudokuItem.optionalSet!!.remove(value)) {
                    affectItems?.add(sudokuItem)
                }
            }
        }
    }

    fun fill(): Boolean {
        var item: SudokuItem
        var fillStep: FillStep
        while (true) {
            if (waitToFillItems.size == 0) {
                return true
            } else {
                item = waitToFillItems.removeAt(0)
                if (!item.fillNumber()) {
                    waitToFillItems.add(item)
                    if (!goBack()) {
                        return false
                    }
                } else {
                    fillStep = getFillStep(item)
                    modifyOptional(item, fillStep.affectItems)
                    fillStack.push(fillStep)
                }
            }
            waitToFillItems.sortBy { it.optionalSet?.size }
        }
    }

    fun getAnswer() = if (fill()) SudokuAnswer(sudokuItems) else null

    fun getAllAnswer(): List<SudokuAnswer> {
        val answers = ArrayList<SudokuAnswer>()
        var answer = getAnswer()
        while (answer != null) {
            answers.add(answer)
            answer = if (goBack()) getAnswer() else null
        }
        return answers
    }

    private fun getFillStep(item: SudokuItem): FillStep {
        return if (stepCache.size == 0) {
            FillStep(item, ArrayList())
        } else {
            val fillStep = stepCache.removeAt(0)
            fillStep.fillItem = item
            fillStep.affectItems.clear()
            fillStep
        }
    }

    private fun goBack(): Boolean {
        while (fillStack.size > 0) {
            val fillStep = fillStack.pop()
            for (item in fillStep.affectItems) {
                item.optionalSet?.add(fillStep.fillItem.value)
            }
            waitToFillItems.add(fillStep.fillItem)
            stepCache.add(fillStep)
            if (fillStep.fillItem.optionalSet!!.size > 0) {
                return true
            }
            fillStep.fillItem.reset()
        }
        return false
    }

    fun reset() {
        while (fillStack.size > 0) {
            goBack()
        }
    }

    data class FillStep(var fillItem: SudokuItem, var affectItems: ArrayList<SudokuItem>)
}