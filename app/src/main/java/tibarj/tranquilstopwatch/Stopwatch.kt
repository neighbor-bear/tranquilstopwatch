package tibarj.tranquilstopwatch

import android.util.Log

class Stopwatch (
    private val _tick: (Long) -> Unit,
    private var _startedAt: Long = 0, // ms
    private var _anteriority: Long = 0, // ms, sum of all start-stop segments durations
    private var _everySecond: Boolean = true,
): AbstractClock("Stopwatch") {
    val startedAt: Long get() = _startedAt
    val anteriority: Long get() = _anteriority
    var everySecond: Boolean
        get() = _everySecond
        set(value) {
            _everySecond = value
        }

    fun start() {
        Log.d(tag, "start")
        if (!isStarted()) {
            _startedAt = System.currentTimeMillis()
            schedule()
        }
    }

    fun stop() {
        Log.d(tag, "stop")
        if (isStarted()) {
            unschedule()
            _anteriority += System.currentTimeMillis() - _startedAt
            _startedAt = 0L
        }
    }

    fun reset() {
        Log.d(tag, "reset")
        unschedule()
        _startedAt = 0L
        _anteriority = 0L
    }

    fun getElapsedMs(): Long {
        return _anteriority + if (isStarted()) System.currentTimeMillis() - _startedAt else 0L
    }

    fun isStarted(): Boolean {
        return 0L != _startedAt
    }

    fun schedule() {
        Log.d(tag, "schedule")
        scheduleIn(getRemainingMs(getElapsedMs()))
    }

    override fun onTick() {
        Log.d(tag, "onTick")
        val elapsedMs = getElapsedMs()
        _tick(elapsedMs)
        scheduleIn(getRemainingMs(elapsedMs))
    }

    private fun getRemainingMs(elapsedMs: Long): Long {
        // remaining ms time until next minute (10ms late for safety)
        return if (_everySecond) 1_010 - elapsedMs % 1_000 else 60_010 - elapsedMs % 60_000
    }
}
