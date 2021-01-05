package com.timepp.sudoku.utils.logger

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.util.*

object Utils {
    /**
     * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
     * in unit tests.
     *
     * @return Stack trace in form of String
     */
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun logLevel(value: Int): String {
        return when (value) {
            Log.VERBOSE -> "VERBOSE"
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    fun toString(any: Any?): String {
        if (any == null) {
            return "null"
        } else if (!any.javaClass.isArray) {
            return any.toString()
        }
        return when (any) {
            is BooleanArray -> Arrays.toString(any)
            is ByteArray -> Arrays.toString(any)
            is CharArray -> Arrays.toString(any)
            is ShortArray -> Arrays.toString(any)
            is IntArray -> Arrays.toString(any)
            is LongArray -> Arrays.toString(any)
            is FloatArray -> Arrays.toString(any)
            is DoubleArray -> Arrays.toString(any)
            is Array<*> -> Arrays.deepToString(any)
            else -> "Couldn't find a correct type for the object"
        }
    }

}