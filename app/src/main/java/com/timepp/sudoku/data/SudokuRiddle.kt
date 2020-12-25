package com.timepp.sudoku.data

class SudokuRiddle(getValue: (index: Int) -> Int) : AbsSudoku() {
    override val sudokuItems: Array<SudokuItem>
    var sudokuSolution: SudokuSolution? = null

    init {
        sudokuItems = Array(SUDO_SIZE) {
            val value = getValue(it)
            SudokuItem(value != SudokuItem.NOT_SURE_NUM,
                it % SUDO_UNIT, it / SUDO_UNIT, value)
        }
    }

    fun isValid(): Boolean {
        for (i in 0 until SUDO_UNIT) {
            if (checkRow(i) < 0 || checkColumn(i) < 0) {
                return false
            }
            if (i % 3 == 0) {
                if ((checkBox(i, 0) < 0) || (checkBox(i, 3) < 0) || (checkBox(i, 6) < 0)) {
                    return false
                }
            }
        }
        return true
    }

    fun checkBox(x: Int, y: Int): Int {
        val startX = x / 3 * 3
        val startY = y / 3 * 3
        return checkUnit { i -> (i / 3 + startY) * 9 + (i % 3 + startX) }
    }

    fun checkColumn(x: Int) = checkUnit { i -> i * 9 + x }

    fun checkRow(y: Int) = checkUnit { i -> y * 9 + i }

    private fun checkUnit(getPosition: (index: Int) -> Int): Int {
        var appearNum = 0
        var num: Int
        var sudokuItem: SudokuItem
        for (i in 0 until SUDO_UNIT) {
            val position = getPosition(i)
            sudokuItem = sudokuItems[position]
            if (sudokuItem.value < 1) {
                if (isAllowUnsureNum) {
                    continue
                } else {
                    return position
                }
            }
            num = 1 shl (sudokuItem.value - 1)
            if (appearNum and num == 0) {
                appearNum = appearNum or num
            } else {
                return position
            }
        }
        return -1
    }

    fun fillSolution(fillIndex: Int) = sudokuSolution?.run {
        if (this@SudokuRiddle.sudokuItems[fillIndex].isFixed) {
            false
        } else {
            val item = this@SudokuRiddle.sudokuItems[fillIndex]
            item.isFixed = true
            item.value = sudokuItems[fillIndex].value
            true
        }
    }

    fun fillSolution() {
        sudokuSolution?.let {
            for ((index, item) in sudokuItems.withIndex()) {
                if (!item.isFixed) {
                    item.value = it.sudokuItems[index].value
                }
            }
        }

    }

}