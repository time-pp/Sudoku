package com.timepp.sudoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.timepp.sudoku.data.Sudoku
import com.timepp.sudoku.view.SudokuView

class MainActivity : AppCompatActivity() {
    val test = arrayOf(0,0,0,0,0,0,0,1,0,4,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,5,
        0,4,0,7,0,0,8,0,0,0,3,0,0,0,0,1,0,9,0,0,0,0,3,0,0,4,0,0,2,0,0,0,5,0,1,0,0,0,0,0,0,0,0,8,0,6,0,0,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sudokuView = findViewById<SudokuView>(R.id.sudoku_view)
        sudokuView.sudoku = Sudoku { i -> test[i] }
    }
}
