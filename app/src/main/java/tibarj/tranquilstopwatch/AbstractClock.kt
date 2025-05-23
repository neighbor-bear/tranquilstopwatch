package tibarj.tranquilstopwatch

import android.os.Handler
import android.os.Looper
import android.util.Log

abstract class AbstractClock(
    protected val tag: String
) {
    protected val _handler = Handler(Looper.getMainLooper())
    protected var _runnable: Runnable? = null
    protected var _isScheduled = false

    init {
        Log.d(tag, "init")
        _runnable = Runnable { onTick() }
    }

    abstract protected fun onTick()

    fun scheduleIn(delayMs: Long) {
        Log.d(tag, "scheduleIn")
        _isScheduled = true
        Log.d(tag, " >scheduled in ${delayMs}ms")
        _handler.postDelayed(_runnable!!, delayMs)
    }

    fun unschedule() {
        Log.d(tag, "unschedule")
        _handler.removeCallbacks(_runnable!!)
        _isScheduled = false
    }
}
