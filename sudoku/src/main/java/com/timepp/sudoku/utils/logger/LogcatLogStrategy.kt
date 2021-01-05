package com.timepp.sudoku.utils.logger

import android.util.Log


/**
 * LogCat implementation for [LogStrategy]
 *
 * This simply prints out all logs to Logcat by using standard [Log] class.
 */
class LogcatLogStrategy : LogStrategy {
    override fun log(priority: Int, tag: String?, message: String) {
        Log.println(priority, tag ?: DEFAULT_TAG, message)
    }

    companion object {
        const val DEFAULT_TAG = "NO_TAG"
    }
}
