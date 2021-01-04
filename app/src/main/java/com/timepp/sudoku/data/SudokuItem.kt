package com.timepp.sudoku.data

import java.util.*

data class SudokuItem constructor(var isFixed: Boolean, val x: Int, val y: Int, var value: Int) {
    companion object {
        const val NOT_SURE_NUM = 0
    }
    val optionalSet: MutableSet<Int>?
    val noteArray: BooleanArray?
    var isError: Boolean = false
    private val tryNum: ArrayList<Int>?
    init {
        if (value != NOT_SURE_NUM) {
            optionalSet = null
            noteArray = null
            tryNum = null
        } else {
            optionalSet = mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
            noteArray = BooleanArray(AbsSudoku.SUDO_UNIT) { false }
            tryNum = ArrayList()
        }
    }

    fun fillNumber(): Boolean {
        return if (optionalSet == null || optionalSet.size < 1) {
            false
        } else {
            value = optionalSet.first()
            optionalSet.remove(value)
            tryNum!!.add(value)
            true
        }
    }

    fun reset() {
        if (optionalSet == null || tryNum == null) {
            return
        }
        value = NOT_SURE_NUM
        optionalSet.addAll(tryNum)
        tryNum.clear()
    }
}