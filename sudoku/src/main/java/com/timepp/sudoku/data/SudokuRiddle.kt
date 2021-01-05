package com.timepp.sudoku.data

class SudokuRiddle(getValue: (index: Int) -> Int) : AbsSudoku() {
    companion object {
        const val FILL_RESULT_RIGHT = 0
        const val FILL_RESULT_WRONG = 1
        const val FILL_RESULT_SOLVED = 2
        const val FILL_RESULT_CAN_NOT_FILL = 3
    }
    override val sudokuItems: Array<SudokuItem>
    var sudokuSolution: SudokuSolution? = null
    val conflictCellPos = arrayOfNulls<Int>(3)
    var waitToFillNum = SUDO_SIZE - SUDOKU_MIN_SIZE
    private set

    init {
        sudokuItems = Array(SUDO_SIZE) {
            val value = getValue(it)
            SudokuItem(value != SudokuItem.NOT_SURE_NUM, it % SUDO_UNIT, it / SUDO_UNIT, value)
        }
    }

    fun isValid(): Boolean {
        for (i in 0 until SUDO_UNIT) {
            if (checkRow(y = i) < 0 || checkColumn(x = i) < 0) {
                return false
            }
            if (i % 3 == 0) {
                if ((checkBox(x = i, y = 0) < 0) || (checkBox(x = i, y = 3) < 0) || (checkBox(x = i, y = 6) < 0)) {
                    return false
                }
            }
        }
        return true
    }

    fun checkBox(position: Int = -1, x: Int, y: Int): Int {
        val startX = x / 3 * 3
        val startY = y / 3 * 3
        return checkUnit(position) { i -> (i / 3 + startY) * 9 + (i % 3 + startX) }
    }

    fun checkColumn(position: Int = -1, x: Int) = checkUnit(position) { i -> i * 9 + x }

    fun checkRow(position: Int = -1, y: Int) = checkUnit(position) { i -> y * 9 + i }

    private fun checkUnit(startPosition: Int, getPosition: (index: Int) -> Int): Int {
        var appearNum = 0
        var num: Int
        var sudokuItem: SudokuItem
        if (startPosition >= 0) {
            sudokuItem = sudokuItems[startPosition]
            if (sudokuItem.value < 1) {
                if (!isAllowUnsureNum) {
                    return startPosition
                }
            }
            appearNum = 1 shl (sudokuItem.value - 1)
        }
        for (i in 0 until SUDO_UNIT) {
            val position = getPosition(i)
            if (position == startPosition) {
                continue
            }
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
            waitToFillNum--
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

    fun resetErrorCell() {
        conflictCellPos[0] = null
        conflictCellPos[1] = null
        conflictCellPos[2] = null
    }

    fun fillNum(position: Int, number: Int): Int {
        if (position < 0) {
            return FILL_RESULT_CAN_NOT_FILL
        }
        val selectItem = sudokuItems[position]
        if (selectItem.isFixed) {
            return FILL_RESULT_CAN_NOT_FILL
        }
        if (selectItem.value != number) {
            selectItem.value = number
            selectItem.isError = number != sudokuSolution?.sudokuItems?.get(position)?.value
            if (!selectItem.isError) {
                waitToFillNum--
            }
        } else {
            selectItem.value = SudokuItem.NOT_SURE_NUM
            if (!selectItem.isError) {
                waitToFillNum++
            } else {
                selectItem.isError = false
            }
        }
        return when {
            waitToFillNum == 0 -> FILL_RESULT_SOLVED
            selectItem.isError -> FILL_RESULT_WRONG
            else -> FILL_RESULT_RIGHT
        }
    }


    fun checkSelectCell(position: Int) {
        resetErrorCell()
        val selectItem = sudokuItems[position]
        if (selectItem.isFixed || selectItem.value == SudokuItem.NOT_SURE_NUM) {
            return
        }
        var result = checkColumn(position, selectItem.x)
        if (result >= 0) {
            conflictCellPos[0] = result
        }
        result = checkRow(position, selectItem.y)
        if (result >= 0) {
            conflictCellPos[1] = result
        }
        result = checkBox(position, selectItem.x, selectItem.y)
        if (result >= 0) {
            conflictCellPos[2] = result
        }
    }

}