package com.timepp.sudoku

import com.timepp.sudoku.data.AbsSudoku
import com.timepp.sudoku.data.Sudoku
import java.io.*
import java.util.zip.ZipInputStream

object SudokuUtils {
    const val DIFFICULTY_EASY = 0
    const val DIFFICULTY_RELAXED = 1
    const val DIFFICULTY_NORMAL = 2
    const val DIFFICULTY_HARD = 3
    const val DIFFICULTY_HELL = 4
    private const val WAIT_FILL_NUM_EASY = 52
    private const val WAIT_FILL_NUM_RELAXED = 39
    private const val WAIT_FILL_NUM_NORMAL = 26
    private const val WAIT_FILL_NUM_HARD = 13
    const val SUDOKU_MIN_SIZE = 17
    private const val READ_SIZE = 50 * SUDOKU_MIN_SIZE
    private const val ZIP_FILE_PATH = "G:\\study\\KotlinWorkSpace\\Sudoku\\assets\\sudoku.dat"
    private const val UNZIP_FILE_PATH = "G:\\study\\KotlinWorkSpace\\Sudoku\\assets\\result.txt"

    fun unzipFile(): Int {
        var sudokuNum = 0
        try {
            val file = File(ZIP_FILE_PATH)
            val bufferedOutputStream = BufferedOutputStream(File(UNZIP_FILE_PATH).outputStream())
            val unzipInputStream = ZipInputStream(file.inputStream())
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

    fun getSudokuByPosition(position: Int): Sudoku {
        val file = File(UNZIP_FILE_PATH)
        val skipLength = (position % (file.length() / SUDOKU_MIN_SIZE)) * SUDOKU_MIN_SIZE
        val fileInputStream = BufferedInputStream(file.inputStream())
        fileInputStream.skip(skipLength)
        val readResult = ByteArray(SUDOKU_MIN_SIZE)
        fileInputStream.read(readResult, 0, SUDOKU_MIN_SIZE)
        fileInputStream.close()
        return Sudoku.buildByFileContent(readResult)
    }

    fun buildSudoku(difficulty: Int): Sudoku {
        val sudoku = getSudokuByPosition((0 .. Int.MAX_VALUE).random())
        if (difficulty == DIFFICULTY_HELL) {
            return sudoku
        }
        val fillNum = when (difficulty) {
            DIFFICULTY_EASY -> WAIT_FILL_NUM_EASY
            DIFFICULTY_RELAXED -> WAIT_FILL_NUM_RELAXED
            DIFFICULTY_NORMAL -> WAIT_FILL_NUM_NORMAL
            DIFFICULTY_HARD -> WAIT_FILL_NUM_HARD
            else -> return sudoku
        }
        val answer = sudoku.getAnswer()
        if (answer == null) {
            sudoku.reset()
            return sudoku
        } else {
            var fillIndex: Int
            for (i in 0 until fillNum) {
                fillIndex = (0 until AbsSudoku.SUDO_SIZE).random()
                while (!answer.setItemFixed(fillIndex)) {
                    fillIndex = (0 until AbsSudoku.SUDO_SIZE).random()
                }
            }
        }
        return answer.buildSudoku()
    }
}