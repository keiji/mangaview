package jp.co.c_lis.bookviewer.android

class Log {

    companion object {

        fun d(tag: String, message: String) {
            if (!BuildConfig.DEBUG) {
                return
            }
            android.util.Log.d(tag, message)
        }
    }
}
