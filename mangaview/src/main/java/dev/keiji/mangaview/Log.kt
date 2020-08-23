package dev.keiji.mangaview

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF

internal class Log {

    companion object {

        fun d(tag: String, message: String) {
            if (!BuildConfig.DEBUG) {
                return
            }
            android.util.Log.d(tag, message)
        }

        fun d(tag: String, message: String, rect: Rect?) {
            if (rect == null) {
                d(tag, "$message, rect is null")
                return
            }
            d(
                tag,
                "$message, Rect(left=${rect.left}, top=${rect.top}, right=${rect.right}, bottom=${rect.bottom})"
            )
        }

        fun d(tag: String, message: String, rect: Rectangle?) {
            if (rect == null) {
                d(tag, "$message, rect is null")
                return
            }
            d(tag, "$message, ${rect}, width=${rect.width}, height=${rect.height}")
        }

        fun d(tag: String, message: String, bitmap: Bitmap?) {
            if (bitmap == null) {
                d(tag, "$message, bitmap is null")
                return
            }
            d(tag, "$message, ${bitmap.width}:${bitmap.height}")
        }

        fun d(tag: String, message: String, rect: RectF?) {
            if (rect == null) {
                d(tag, "$message, rect is null")
                return
            }
            d(
                tag, "$message," +
                        " RectF(left=${rect.left}, top=${rect.top}, right=${rect.right}, bottom=${rect.bottom})," +
                        " width=${rect.width()}, height=${rect.height()}"
            )
        }
    }
}
