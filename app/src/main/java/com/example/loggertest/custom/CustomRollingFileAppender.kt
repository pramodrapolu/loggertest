package com.example.loggertest.custom

import android.content.Context
import android.util.Log
import ch.qos.logback.core.recovery.ResilientFileOutputStream
import ch.qos.logback.core.rolling.RollingFileAppender
import java.io.FileOutputStream
import java.io.OutputStream
import javax.crypto.CipherOutputStream

/**
 * Modified version of [RollingFileAppender].
 *
 * When the new file stream is about to begin, we append the header.
 */
class CustomRollingFileAppender<E> constructor(context: Context) : RollingFileAppender<E>() {

    val logProcessor: LogProcessor

    init {
        logProcessor = LogProcessor(context)

    }

    /**
     * Called when a new [OutputStream] is to be used be the [RollingFileAppender].
     * Overridden such that we encrypt the stream.
     */
    override fun setOutputStream(outputStream: OutputStream) {
        synchronized(lock) {
            // if setOutputStream is called from [RollingFileAppender.openFile]
            // wrap outputStream with ptsEncryptor's outputStream
            if (outputStream is ResilientFileOutputStream) {
                val cos: CipherOutputStream = logProcessor.encrypt(outputStream)
                super.setOutputStream(cos)
            } else {
                super.setOutputStream(outputStream)
            }
        }
    }

}
