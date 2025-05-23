package tibarj.tranquilstopwatch

import android.util.Log

class Clock(
    private val _tick: () -> Unit,
): AbstractClock("Clock") {

    fun start() {
        Log.d(tag, "start")
        if (_isScheduled) {
            Log.w(tag, "already scheduled")
        } else {
            schedule()
        }
    }

    fun stop() {
        Log.d(tag, "stop")
        if (!_isScheduled) {
            Log.w(tag, "already unscheduled")
        }
        unschedule()
    }

    private fun schedule() {
        Log.d(tag, "schedule")
        // remaining ms time until next minute (10ms late for safety)
        val remainingMs = 60_010 - System.currentTimeMillis() % 60_000
        scheduleIn(remainingMs)
    }

    override fun onTick() {
        Log.d(tag, "onTick")
        _tick()
        schedule()
    }
}
