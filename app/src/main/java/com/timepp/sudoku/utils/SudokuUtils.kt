package com.timepp.sudoku.utils

import android.content.Context
import com.timepp.sudoku.constants.TAG_SUDOKU_UTILS
import com.timepp.sudoku.data.AbsSudoku
import com.timepp.sudoku.data.SudokuSolver
import com.timepp.sudoku.data.SudokuItem
import com.timepp.sudoku.data.SudokuRiddle
import com.timepp.sudoku.utils.logger.Logger
import java.io.*
import java.util.zip.ZipInputStream

object SudokuUtils {
    const val DIFFICULTY_EASY = 0
    const val DIFFICULTY_RELAXED = 1
    const val DIFFICULTY_NORMAL = 2
    const val DIFFICULTY_HARD = 3
    const val DIFFICULTY_HELL = 4
    private const val FILL_NUM_EASY = 32
    private const val FILL_NUM_RELAXED = 24
    private const val FILL_NUM_NORMAL = 16
    private const val FILL_NUM_HARD = 8
    private const val FILL_NUM_HELL = 0
    private const val SUDOKU_MIN_SIZE = 17
    private const val READ_SIZE = 50 * SUDOKU_MIN_SIZE
    private const val ZIP_FILE_PATH = "sudoku.dat"
    private const val UNZIP_FILE_PATH = "result.txt"

    fun unzipFile(context: Context): Int {
        val unZipFile = File(context.filesDir, UNZIP_FILE_PATH)
        if (unZipFile.exists()) {
            return 0
        }
        var sudokuNum = 0
        try {
            val bufferedOutputStream = BufferedOutputStream(unZipFile.outputStream())
            val unzipInputStream = ZipInputStream(context.assets.open(ZIP_FILE_PATH))
            if (unzipInputStream.nextEntry != null) {
                val readBuff = ByteArray(READ_SIZE)
                var length = unzipInputStream.read(readBuff)
                var totalLength = 0
                while (length > 0) {
                    bufferedOutputStream.write(readBuff, 0, length)
                    totalLength += length
                    length = unzipInputStream.read(readBuff)
                }
                sudokuNum = totalLength / SUDOKU_MIN_SIZE
            }
            unzipInputStream.closeEntry()
            unzipInputStream.close()
            bufferedOutputStream.flush()
            bufferedOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            return sudokuNum
        }
    }

    private fun getSudokuRiddleByPosition(context: Context, position: Int): SudokuRiddle {
        val file = File(context.filesDir, UNZIP_FILE_PATH)
        val skipLength = (position % (file.length() / SUDOKU_MIN_SIZE)) * SUDOKU_MIN_SIZE
        val fileInputStream = BufferedInputStream(file.inputStream())
        fileInputStream.skip(skipLength)
        val readResult = ByteArray(SUDOKU_MIN_SIZE)
        fileInputStream.read(readResult, 0, SUDOKU_MIN_SIZE)
        fileInputStream.close()
        return buildRiddleByFileContent(readResult)
    }

    fun buildSudokuRiddle(context: Context, difficulty: Int): SudokuRiddle {
        val startBuildTime = System.currentTimeMillis()
        var lastStepFinishTime = startBuildTime
        val sudokuRiddle = getSudokuRiddleByPosition(context, (0 .. Int.MAX_VALUE).random())
        var currentTime = System.currentTimeMillis()
        Logger.t(TAG_SUDOKU_UTILS).d("finish read sudoku from file,use time = ${currentTime - lastStepFinishTime}")
        lastStepFinishTime = currentTime
        val solution = SudokuSolver(sudokuRiddle).getSolution()
        currentTime = System.currentTimeMillis()
        Logger.t(TAG_SUDOKU_UTILS).d("finish find sudoku solution, use time = ${currentTime - lastStepFinishTime}")
        lastStepFinishTime = currentTime
        if (solution == null) {
            Logger.t(TAG_SUDOKU_UTILS).e("can not find sudoku solution, sudoku is $sudokuRiddle")
            return buildSudokuRiddle(context, difficulty)
        }
        sudokuRiddle.sudokuSolution = solution
        if (difficulty != DIFFICULTY_HELL) {
            fillSomeCell(sudokuRiddle, difficulty)
        }
        currentTime = System.currentTimeMillis()
        Logger.t(TAG_SUDOKU_UTILS).d("finish build sudoku, use time = ${currentTime - lastStepFinishTime}")
        Logger.t(TAG_SUDOKU_UTILS).d("finish all step, use all time = ${currentTime - startBuildTime}")
        return sudokuRiddle
    }

    private fun fillSomeCell(sudokuRiddle: SudokuRiddle, difficulty: Int) {
        val fillNum = when (difficulty) {
            DIFFICULTY_EASY -> FILL_NUM_EASY
            DIFFICULTY_RELAXED -> FILL_NUM_RELAXED
            DIFFICULTY_NORMAL -> FILL_NUM_NORMAL
            DIFFICULTY_HARD -> FILL_NUM_HARD
            else -> return
        }
        var fillIndex: Int

        for (i in 0 until fillNum) {
            fillIndex = (0 until AbsSudoku.SUDO_SIZE).random()
            while (!sudokuRiddle.fillSolution(fillIndex)!!) {
                fillIndex = (0 until AbsSudoku.SUDO_SIZE).random()
            }
        }
    }

    fun buildRiddleByValues(sudokuValues: IntArray): SudokuRiddle {
        if (sudokuValues.size != AbsSudoku.SUDO_SIZE) {
            throw IllegalArgumentException("values size must be ${AbsSudoku.SUDO_SIZE}")
        }
        return SudokuRiddle { pos -> sudokuValues[pos]}
    }

    private fun buildRiddleByFileContent(sudokuValues: ByteArray): SudokuRiddle {
        if (sudokuValues.size != SUDOKU_MIN_SIZE) {
            throw IllegalArgumentException("values size must be $SUDOKU_MIN_SIZE")
        }
        var currentValueIndex = 0
        var currentValue = sudokuValues[currentValueIndex].toUByte().toInt()
        var valueIndex = currentValue / 10
        return SudokuRiddle { pos ->
            if (pos != valueIndex) {
                SudokuItem.NOT_SURE_NUM
            } else {
                val result = currentValue % 10
                if (++currentValueIndex != SUDOKU_MIN_SIZE) {
                    currentValue = sudokuValues[currentValueIndex].toUByte().toInt()
                    valueIndex += currentValue / 10 + 1
                }
                result
            }
        }
    }
}