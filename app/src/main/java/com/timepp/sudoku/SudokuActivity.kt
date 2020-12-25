package com.timepp.sudoku

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.timepp.sudoku.utils.SudokuUtils
import kotlinx.android.synthetic.main.activity_sudoku.*

class SudokuActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)
        buildSudoku1.setOnClickListener(this)
        buildSudoku2.setOnClickListener(this)
        buildSudoku3.setOnClickListener(this)
        buildSudoku4.setOnClickListener(this)
        buildSudoku5.setOnClickListener(this)
        fill1.setOnClickListener(this)
        fill2.setOnClickListener(this)
        fill3.setOnClickListener(this)
        fill4.setOnClickListener(this)
        fill5.setOnClickListener(this)
        fill6.setOnClickListener(this)
        fill7.setOnClickListener(this)
        fill8.setOnClickListener(this)
        fill9.setOnClickListener(this)
        fillSudoku.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        view?.let{
            val difficulty = when (it.id) {
                R.id.buildSudoku1 -> 0
                R.id.buildSudoku2 -> 1
                R.id.buildSudoku3 -> 2
                R.id.buildSudoku4 -> 3
                R.id.buildSudoku5 -> 4
                R.id.fillSudoku -> {
                    sudokuView.sudokuRiddle?.fillSolution()
                    sudokuView.invalidate()
                    return
                }
                else -> -1
            }
            if (difficulty >= 0) {
                sudokuView.sudokuRiddle = SudokuUtils.buildSudokuRiddle(this, difficulty)
                return
            }
            val number = when (it.id) {
                R.id.fill1 -> 1
                R.id.fill2 -> 2
                R.id.fill3 -> 3
                R.id.fill4 -> 4
                R.id.fill5 -> 5
                R.id.fill6 -> 6
                R.id.fill7 -> 7
                R.id.fill8 -> 8
                R.id.fill9 -> 9
                else -> return
            }
            sudokuView.fillNum(number)
        }
    }
}
