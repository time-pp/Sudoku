package com.timepp.sudoku

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.timepp.sudoku.utils.SudokuUtils

class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Thread { SudokuUtils.unzipFile(this)}.start()
        startActivity(Intent(this, MainActivity::class.java))
    }

}