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

}