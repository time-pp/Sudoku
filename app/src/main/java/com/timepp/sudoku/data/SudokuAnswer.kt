package com.timepp.sudoku.data

class SudokuAnswer(sudokuItems: Array<SudokuItem>) : AbsSudoku() {
    override val sudokuItems: Array<SudokuItem>
    override val isAllowUnsureNum: Boolean = false
    init {
        if (sudokuItems.size != SUDO_SIZE) {
            throw IllegalArgumentException("SudokuItems's size(${sudokuItems.size}) must be 81")
        }
        this.sudokuItems = Array(sudokuItems.size) { i ->
            sudokuItems[i].apply {
                return@Array SudokuItem(isFixed, x, y, value)
            }
        }
    }

    fun setItemFixed(index: Int) = when {
        index !in (0 until SUDO_SIZE) -> false
        sudokuItems[index].isFixed -> false
        else -> {
            sudokuItems[index].isFixed = true
            true
        }
    }

    fun buildSudoku(): Sudoku {
        return Sudoku() {pos -> if (sudokuItems[pos].isFixed) sudokuItems[pos].value else SudokuItem.NOT_SURE_NUM}
    }
}