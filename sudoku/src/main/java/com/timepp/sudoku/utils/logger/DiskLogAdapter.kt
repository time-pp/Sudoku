package com.timepp.sudoku.utils.logger

import android.content.Context


/**
 * This is used to saves log messages to the disk.
 * By default it uses [CsvFormatStrategy] to translates text message into CSV format.
 */
class DiskLogAdapter(private val formatStrategy: FormatStrategy) : LogAdapter {
    constructor(context: Context): this(CsvFormatStrategy.newBuilder().build(context))

    override fun isLoggable(priority: Int, tag: String?): Boolean {
        return true
    }

    override fun log(priority: Int, tag: String?, message: String) {
        formatStrategy.log(priority, tag, message)
    }
}
