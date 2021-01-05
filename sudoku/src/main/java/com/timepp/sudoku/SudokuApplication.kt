package com.timepp.sudoku

import android.app.Application
import com.timepp.sudoku.utils.logger.AndroidLogAdapter
import com.timepp.sudoku.utils.logger.Logger

class SudokuApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}