package com.timepp.sudoku.data

class SudokuSolution(sudokuItems: Array<SudokuItem>): AbsSudoku() {
    override val sudokuItems: Array<SudokuItem>
    override val isAllowUnsureNum: Boolean = false
    init {
        if (sudokuItems.size != SUDO_SIZE) {
            throw IllegalArgumentException("SudokuItems's size(${sudokuItems.size}) must be $SUDO_SIZE")
        }
        this.sudokuItems = Array(SUDO_SIZE) { sudokuItems[it].copy(isFixed = true) }
    }
}