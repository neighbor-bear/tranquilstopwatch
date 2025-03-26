package tibarj.tranquilstopwatch

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import tibarj.tranquilstopwatch.databinding.StopwatchFragmentBinding
import java.util.concurrent.TimeUnit


class StopwatchFragment : Fragment() {
    private val tag: String = "StopwatchFragment"
    private lateinit var _sharedPreferences: SharedPreferences
    private var _binding: StopwatchFragmentBinding? = null
    private var _runnable: Runnable? = null
    private val _handler = Handler(Looper.getMainLooper())
    private var _startedAt: Long = 0 // ms
    private var _anteriority: Long = 0 // ms, sum of all start-stop segments durations
    private var _showSeconds: Boolean = true

    // This property is only valid between onCreateView and onDestroyView.
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView")
        _sharedPreferences = requireContext().getSharedPreferences(
            "StopwatchFragment",
            Context.MODE_PRIVATE
        )
        _startedAt = _sharedPreferences.getLong("startedAt", 0L)
        _anteriority = _sharedPreferences.getLong("anteriority", 0L)
        _binding = StopwatchFragmentBinding.inflate(inflater, container, false)
        _runnable = Runnable {
            onTimerTick()
        }
        initTapListeners()
        return binding.root
    }

    fun saveInstanceState() {
        Log.d(tag, "saveInstanceState")

        with(_sharedPreferences.edit()) {
            putLong("startedAt", _startedAt)
            putLong("anteriority", _anteriority)
            apply()
        }
        logState()
    }

    private fun loadPref() {
        Log.d(tag, "applyPref")
        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        _showSeconds = pref.getBoolean(
            getString(R.string.stopwatch_show_seconds_key),
            resources.getBoolean(R.bool.default_stopwatch_show_seconds)
        )

        val fontFamily = "sans-serif" + if (pref.getBoolean(
            getString(R.string.stopwatch_font_thin_key),
            resources.getBoolean(R.bool.default_stopwatch_font_thin)
        )) "-thin" else ""
        Log.d(tag, "setFontFamily " + fontFamily)
        binding.stopwatch.typeface = Typeface.create(fontFamily, Typeface.NORMAL)

        val opacity = pref.getInt(
            getString(R.string.stopwatch_opacity_key),
            resources.getInteger(R.integer.default_stopwatch_opacity)
        )
        Log.d(tag, "setStopwatchOpacity " + opacity.toString())
        binding.stopwatch.alpha = opacity.toFloat() / 20f

        val size = pref.getInt(getString(R.string.stopwatch_size_key), resources.getInteger(R.integer.default_stopwatch_size))
        Log.d(tag, "setStopwatchSize " + size.toString())
        binding.stopwatch.textSize = size.toFloat()
    }

    // visible but not interactable
    override fun onStart() {
        Log.d(tag, "onStart")
        super.onStart()

        loadPref()
        logState()

        display()
        if (isStarted()) {
            keepScreenOn()
            setColor(R.color.white)
            schedule()
        } else {
            setColor(R.color.red)
        }
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()

        unschedule()
        unkeepScreenOn()
    }

    private fun initTapListeners() {
        Log.d(tag, "setTapListeners")
        binding.stopwatch.setOnClickListener {
            Log.d(tag, "OnClickTimeview")
            toggle()
        }
        binding.stopwatch.setOnLongClickListener {
            Log.d(tag, "onLongClickTimeview")
            reset()
            true
        }
    }

    private fun isStarted(): Boolean {
        return 0L != _startedAt
    }

    private fun start() {
        Log.d(tag, "start")
        _startedAt = System.currentTimeMillis()
        saveInstanceState()
        keepScreenOn()
        setColor(R.color.white)
        display()
        schedule()
        this.logState()
    }

    private fun stop() {
        Log.d(tag, "stop")
        unschedule()
        _anteriority += System.currentTimeMillis() - _startedAt
        _startedAt = 0L
        saveInstanceState()
        unkeepScreenOn()
        setColor(R.color.red)
        display() // show seconds because showing only when stopped
        this.logState()
    }

    private fun toggle() {
        Log.d(tag, "toggle")
        if (isStarted()) {
            stop()
        } else {
            start()
        }
    }

    private fun reset() {
        Log.d(tag, "reset")
        unschedule()
        _startedAt = 0L
        _anteriority = 0L
        saveInstanceState()
        unkeepScreenOn()
        setColor(R.color.red)
        setClock(0, 0, 0)
    }

    private fun display() {
        Log.d(tag, "display")
        val elapsed = TimeUnit.MILLISECONDS.toSeconds(
            _anteriority + if (isStarted()) System.currentTimeMillis() - _startedAt else 0L
        )
        Log.d(tag, " >elapsed=$elapsed")

        setClock(
            TimeUnit.SECONDS.toHours(elapsed).toInt(),
            TimeUnit.SECONDS.toMinutes(elapsed).toInt() % 60,
            elapsed.toInt() % 60
        )
    }

    private fun setClock(h: Int, m: Int, s: Int) {
        Log.d(tag, "setClock")
        _binding?.stopwatch?.text =
            if (_showSeconds) getString(R.string.clock_h_mm_ss, h, m, s)
            else getString(R.string.clock_h_mm, h, m)
    }

    private fun setColor(@ColorRes color: Int) {
        Log.d(tag, "setColor")
        val colorI = ContextCompat.getColor(requireActivity(), color)
        _binding?.stopwatch?.setTextColor(colorI)
    }

    private fun keepScreenOn() {
        Log.d(tag, "keepScreenOn")
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun unkeepScreenOn() {
        Log.d(tag, "unkeepScreenOn")
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun logState() {
        Log.d(tag, "state={")
        Log.d(tag, "  _startedAt=" + _startedAt.toString())
        Log.d(tag, "  _anteriority=" + _anteriority.toString())
        Log.d(tag, "  _showSeconds=" + _showSeconds.toString())
        Log.d(tag, "  isStarted=" + isStarted().toString())
        Log.d(tag, "}")
    }

//    private fun isScheduled(): Boolean {
//        return true == _runnable?.let { _handler.hasCallbacks(it) }
//    }

    private fun schedule() {
        Log.d(tag, "schedule")
        val elapsed = (_anteriority + System.currentTimeMillis() - _startedAt)
        // remaining ms time until next minute (10ms of safety)
        val remainingMs = if (_showSeconds) 1_010 - elapsed % 1_000
            else 60_010 - elapsed % 60_000
        Log.d(tag, " >scheduled in ${remainingMs}ms")
        _handler.postDelayed(_runnable!!, remainingMs)
    }

    private fun unschedule() {
        Log.d(tag, "unschedule")
        _runnable?.let {
            _handler.removeCallbacks(it)
        }
    }

    private fun onTimerTick() {
        Log.d(tag, "onTimerTick")
        if (null === _binding) {
            return
        }
        display()
        schedule()
    }
}