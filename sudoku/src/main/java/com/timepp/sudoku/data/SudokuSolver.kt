package com.timepp.sudoku.data

import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

class SudokuSolver(getSudokuItem: (index: Int) -> SudokuItem) : AbsSudoku() {
    override val sudokuItems: Array<SudokuItem>
    private val waitToFillItems = ArrayList<SudokuItem>()
    private val fillStack = Stack<FillStep>()
    private val stepCache: ArrayList<FillStep>

    init {
        val sureItems = ArrayList<SudokuItem>()
        sudokuItems = Array(SUDO_SIZE) { i ->
            val sudoItem = getSudokuItem(i)
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

    constructor(sudokuRiddle: SudokuRiddle) : this({ sudokuRiddle.sudokuItems[it].copy() })

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

    private fun fill(): Boolean {
        var item: SudokuItem
        var fillStep: FillStep
        val test = System.currentTimeMillis()
        while (true) {
            if (waitToFillItems.size == 0) {
                Log.d("Sudoku", "fill use time = ${System.currentTimeMillis() - test}")
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


    fun getSolution() = if (fill()) SudokuSolution(sudokuItems) else null

    fun getAllSolution(): List<SudokuSolution> {
        val solutions = ArrayList<SudokuSolution>()
        var solution = getSolution()
        while (solution != null) {
            solutions.add(solution)
            solution = if (goBack()) getSolution() else null
        }
        return solutions
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

    data class FillStep(var fillItem: SudokuItem, var affectItems: ArrayList<SudokuItem>)
}