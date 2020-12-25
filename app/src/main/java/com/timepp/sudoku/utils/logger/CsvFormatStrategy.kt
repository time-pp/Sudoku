package com.timepp.sudoku.utils.logger

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 */
class CsvFormatStrategy private constructor(builder: Builder) : FormatStrategy {
    private val date: Date
    private val dateFormat: SimpleDateFormat
    private val logStrategy: LogStrategy
    private val tag: String?

    init {
        date = builder.date!!
        dateFormat = builder.dateFormat!!
        logStrategy = builder.logStrategy!!
        tag = builder.tag
    }

    companion object {
        private val NEW_LINE = System.getProperty("line.separator")
        private const val NEW_LINE_REPLACEMENT = " <br> "
        private const val SEPARATOR = ","
        fun newBuilder(): Builder {
            return Builder()
        }
    }
    override fun log(priority: Int, tag: String?, message: String) {
        var logMessage = message
        val formatTag = formatTag(tag)
        date.time = System.currentTimeMillis()
        val builder = StringBuilder()

        // machine-readable date/time
        builder.append(date.time.toString())

        // human-readable date/time
        builder.append(SEPARATOR)
        builder.append(dateFormat.format(date))

        // level
        builder.append(SEPARATOR)
        builder.append(Utils.logLevel(priority))

        // tag
        builder.append(SEPARATOR)
        builder.append(formatTag)

        // message
        if (logMessage.contains(NEW_LINE!!)) {
            // a new line would break the CSV format, so we replace it here
            logMessage = logMessage.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT)
        }
        builder.append(SEPARATOR)
        builder.append(logMessage)

        // new line
        builder.append(NEW_LINE)
        logStrategy.log(priority, formatTag, builder.toString())
    }

    private fun formatTag(tag: String?) = if (!tag.isNullOrEmpty() && !TextUtils.equals(this.tag, tag))
        this.tag + "-" + tag else this.tag

    class Builder internal constructor(){
        var date: Date? = null
        var dateFormat: SimpleDateFormat? = null
        var logStrategy: LogStrategy? = null
        var tag: String? = "SUDOKU"
        fun date(value: Date?): Builder {
            date = value
            return this
        }

        fun dateFormat(value: SimpleDateFormat?): Builder {
            dateFormat = value
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

        fun build(context: Context): CsvFormatStrategy {
            if (date == null) {
                date = Date()
            }
            if (dateFormat == null) {
                dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
            }
            if (logStrategy == null) {
                val diskPath = context.filesDir.absolutePath
                val folder = diskPath + File.separatorChar + "logger"
                val ht = HandlerThread("AndroidFileLogger.$folder")
                ht.start()
                val handler: Handler = DiskLogStrategy.WriteHandler(ht.looper, folder, MAX_BYTES)
                logStrategy = DiskLogStrategy(handler)
            }
            return CsvFormatStrategy(this)
        }

        companion object {
            private const val MAX_BYTES = 500 * 1024 // 500K averages to a 4000 lines per file
        }
    }
}
