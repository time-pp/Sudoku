package com.timepp.sudoku

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.timepp.sudoku.utils.SudokuUtils
import com.timepp.sudoku.view.SudokuView

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val sudokuView by lazy { findViewById<SudokuView>(R.id.sudoku_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.build_sudoku1).setOnClickListener(this)
        findViewById<View>(R.id.build_sudoku2).setOnClickListener(this)
        findViewById<View>(R.id.build_sudoku3).setOnClickListener(this)
        findViewById<View>(R.id.build_sudoku4).setOnClickListener(this)
        findViewById<View>(R.id.build_sudoku5).setOnClickListener(this)
        findViewById<View>(R.id.fill_sudoku).setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        view?.let{
            val difficulty = when (it.id) {
                R.id.build_sudoku1 -> 0
                R.id.build_sudoku2 -> 1
                R.id.build_sudoku3 -> 2
                R.id.build_sudoku4 -> 3
                R.id.build_sudoku5 -> 4
                R.id.fill_sudoku -> {
                    sudokuView.sudoku?.fill()
                    sudokuView.invalidate()
                    return
                }
                else -> return
            }
            sudokuView.sudoku = SudokuUtils.buildSudoku(this, difficulty)
        }
    }
}
