package com.timepp.sudoku.utils.logger

import android.text.TextUtils
import kotlin.math.min

/**
 * Draws borders around the given log message along with additional information such as :
 *
 * <ul>
 *   <li>Thread information</li>
 *   <li>Method stack trace</li>
 * </ul>
 *
 * <pre>
 *  ┌──────────────────────────
 *  │ Method stack history
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Thread information
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *  │ Log message
 *  └──────────────────────────
 * </pre>
 *
 * <h3>Customize</h3>
 * <pre><code>
 *   FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
 *       .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
 *       .methodCount(0)         // (Optional) How many method line to show. Default 2
 *       .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
 *       .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
 *       .tag("My custom tag")   // (Optional) Global tag for every log. Default SUDOKU
 *       .build();
 * </code></pre>
 */
class PrettyFormatStrategy private constructor(builder: Builder) : FormatStrategy {
    private var methodCount = 0
    private var methodOffset = 0
    private var showThreadInfo = false
    private var logStrategy: LogStrategy
    private var tag: String? = null

    init {
        methodCount = builder.methodCount
        methodOffset = builder.methodOffset
        showThreadInfo = builder.showThreadInfo
        logStrategy = builder.logStrategy!!
        tag = builder.tag
    }

    override fun log(priority: Int, tag: String?, message: String) {
        val formatTag = formatTag(tag)
        logTopBorder(priority, formatTag)
        logHeaderContent(priority, formatTag, methodCount)

        //get bytes of message with system's default charset (which is UTF-8 for Android)
        val bytes = message.toByteArray()
        val length = bytes.size
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                logDivider(priority, formatTag)
            }
            logContent(priority, formatTag, message)
            logBottomBorder(priority, formatTag)
            return
        }
        if (methodCount > 0) {
            logDivider(priority, formatTag)
        }
        var i = 0
        while (i < length) {
            val count = min(length - i, CHUNK_SIZE)
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(priority, formatTag, String(bytes, i, count))
            i += CHUNK_SIZE
        }
        logBottomBorder(priority, formatTag)
    }

    private fun logTopBorder(logType: Int, tag: String?) {
        logChunk(logType, tag, TOP_BORDER)
    }

    private fun logHeaderContent(logType: Int, tag: String?, methodCount: Int) {
        var count = methodCount
        val trace = Thread.currentThread().stackTrace
        if (showThreadInfo) {
            logChunk(logType, tag, "$HORIZONTAL_LINE Thread: ${Thread.currentThread().name}")
            logDivider(logType, tag)
        }
        var level = ""
        val stackOffset = getStackOffset(trace) + methodOffset

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (count + stackOffset > trace.size) {
            count = trace.size - stackOffset - 1
        }
        for (i in count downTo 1) {
            val stackIndex = i + stackOffset
            if (stackIndex >= trace.size) {
                continue
            }
            val builder = StringBuilder()
            builder.append(HORIZONTAL_LINE)
                .append(' ')
                .append(level)
                .append(getSimpleClassName(trace[stackIndex].className))
                .append(".")
                .append(trace[stackIndex].methodName)
                .append(" ")
                .append(" (")
                .append(trace[stackIndex].fileName)
                .append(":")
                .append(trace[stackIndex].lineNumber)
                .append(")")
            level += "   "
            logChunk(logType, tag, builder.toString())
        }
    }

    private fun logBottomBorder(logType: Int, tag: String?) {
        logChunk(logType, tag, BOTTOM_BORDER)
    }

    private fun logDivider(logType: Int, tag: String?) {
        logChunk(logType, tag, MIDDLE_BORDER)
    }

    private fun logContent(logType: Int, tag: String?, chunk: String) {
        val lines = chunk.split(NEW_LINE!!.toRegex()).toTypedArray()
        for (line in lines) {
            logChunk(logType, tag, "$HORIZONTAL_LINE $line")
        }
    }

    private fun logChunk(priority: Int, tag: String?, chunk: String) {
        logStrategy.log(priority, tag, chunk)
    }

    private fun getSimpleClassName(name: String): String {
        val lastIndex = name.lastIndexOf(".")
        return name.substring(lastIndex + 1)
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        var i = MIN_STACK_OFFSET
        while (i < trace.size) {
            val e = trace[i]
            val name = e.className
            if (name != LoggerPrinter::class.java.name && name != Logger::class.java.name) {
                return --i
            }
            i++
        }
        return -1
    }

    private fun formatTag(tag: String?) = if (!tag.isNullOrEmpty() && !TextUtils.equals(this.tag, tag))
        this.tag + "-" + tag else this.tag

    class Builder {
        var methodCount = 2
        var methodOffset = 0
        var showThreadInfo = true
        var logStrategy: LogStrategy? = null
        var tag: String? = "SUDOKU"
        fun methodCount(value: Int): Builder {
            methodCount = value
            return this
        }

        fun methodOffset(value: Int): Builder {
            methodOffset = value
            return this
        }

        fun showThreadInfo(value: Boolean): Builder {
            showThreadInfo = value
            return this
        }

        fun logStrategy(value: LogStrategy?): Builder {
            logStrategy = value
            return this
        }

        fun tag(tag: String?): Builder {
            this.tag = tag
            return this
        }

        fun build(): PrettyFormatStrategy {
            if (logStrategy == null) {
                logStrategy = LogcatLogStrategy()
            }
            return PrettyFormatStrategy(this)
        }
    }

    companion object {
        /**
         * Android's max limit for a log entry is ~4076 bytes,
         * so 4000 bytes is used as chunk size since default charset
         * is UTF-8
         */
        private const val CHUNK_SIZE = 4000

        /**
         * The minimum stack trace index, starts at this class after two native calls.
         */
        private const val MIN_STACK_OFFSET = 5

        /**
         * Drawing toolbox
         */
        private const val TOP_LEFT_CORNER = '┌'
        private const val BOTTOM_LEFT_CORNER = '└'
        private const val MIDDLE_CORNER = '├'
        private const val HORIZONTAL_LINE = '│'
        private const val DOUBLE_DIVIDER = "────────────────────────────────────────────────────────"
        private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private const val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val BOTTOM_BORDER = BOTTOM_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER + SINGLE_DIVIDER
        private val NEW_LINE = System.getProperty("line.separator")

        fun newBuilder(): Builder {
            return Builder()
        }
    }
}