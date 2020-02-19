package com.timepp.sudoku.data

abstract class AbsSudoku {
    companion object {
        // 数独的大小
        const val SUDO_SIZE = 81
        // 数独的一个单元的大小，即行/列/宫格的大小
        const val SUDO_UNIT = 9
        // use for toString()
        private const val SEPARATOR = ','
        // use for toString()
        private const val NEW_LINE = '\n'
    }

    abstract val sudokuItems: Array<SudokuItem>
    protected open val isAllowUnsureNum: Boolean = true

    override fun toString(): String {
        val sb = StringBuilder()
        for (item in sudokuItems) {
            sb.append(item.value)
            if (item.x == SUDO_UNIT - 1) {
                sb.append(NEW_LINE)
            } else {
                sb.append(SEPARATOR)
            }
        }
        return sb.toString()
    }

    fun isValid(): Boolean {
        for (i in 0 until SUDO_UNIT) {
            if (!checkRow(i) || !checkColumn(i)) {
                return false
            }
            if (i % 3 == 0) {
                if ((!checkBox(i, 0)) || (!checkBox(i, 3) || (!checkBox(i, 6)))) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkBox(x: Int, y: Int): Boolean {
        val startX = x / 3 * 3
        val startY = y / 3 * 3
        return checkUnit { i -> (i / 3 + startY) * 9 + (i % 3 + startX) }
    }

    private fun checkColumn(x: Int): Boolean {
        return checkUnit { i -> i * 9 + x }
    }

    private fun checkRow(y: Int): Boolean {
        return checkUnit { i -> y * 9 + i }
    }

    private fun checkUnit(getPosition: (index: Int) -> Int): Boolean {
        var appearNum = 0
        var num: Int
        var sudokuItem: SudokuItem
        for (i in 0 until SUDO_UNIT) {
            sudokuItem = sudokuItems[getPosition(i)]
            if (sudokuItem.value < 1) {
                if (isAllowUnsureNum) {
                    continue
                } else {
                    return false
                }
            }
            num = 1 shl (sudokuItem.value - 1)
            if (appearNum and num == 0) {
                appearNum = appearNum or num
            } else {
                return false
            }
        }
        return true
    }
}