package jp.co.c_lis.bookviewer.android

import android.graphics.Rect

class Log {

    companion object {

        fun d(tag: String, message: String) {
            if (!BuildConfig.DEBUG) {
                return
            }
            android.util.Log.d(tag, message)
        }

        fun d(tag: String, message: String, rect: Rect) {
            d(
                tag,
                "$message, Rect(left=${rect.left}, top=${rect.top}, right=${rect.right}, bottom=${rect.bottom})"
            )
        }

        fun d(tag: String, message: String, rect: Rectangle) {
            d(tag, "$message, ${rect}")

        }
    }
}
