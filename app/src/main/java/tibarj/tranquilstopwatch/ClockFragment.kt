package tibarj.tranquilstopwatch

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import tibarj.tranquilstopwatch.databinding.ClockFragmentBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClockFragment : Fragment() {
    private val tag: String = "ClockFragment"
    private var _binding: ClockFragmentBinding? = null
    private var _runnable: Runnable? = null
    private val _handler = Handler(Looper.getMainLooper())
    private var _enabled: Boolean = true

    // This property is only valid between onCreateView and onDestroyView.
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView")
        _binding = ClockFragmentBinding.inflate(inflater, container, false)
        _runnable = Runnable {
            onTimerTick()
        }
        return binding.root
    }

    private fun loadPref() {
        Log.d(tag, "applyPref")

        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        _enabled = pref.getBoolean(
            getString(R.string.clock_enabled_key),
            resources.getBoolean(R.bool.default_clock_enabled)
        )

        val fontFamily = "sans-serif" + if (pref.getBoolean(
                getString(R.string.clock_font_thin_key),
                resources.getBoolean(R.bool.default_clock_font_thin)
            )) "-thin" else ""
        Log.d(tag, "setFontFamily " + fontFamily)
        binding.clock.typeface = Typeface.create(fontFamily, Typeface.NORMAL)

        val opacity = pref.getInt(
            getString(R.string.clock_opacity_key),
            resources.getInteger(R.integer.default_clock_opacity)
        )
        Log.d(tag, "setClockOpacity " + opacity.toString())
        binding.clock.alpha = opacity.toFloat() / 20f

        val size = pref.getInt(getString(R.string.clock_size_key), resources.getInteger(R.integer.default_stopwatch_size))
        Log.d(tag, "setClockSize " + size.toString())
        binding.clock.textSize = size.toFloat()
    }

    // visible but not interactable
    override fun onStart() {
        Log.d(tag, "onStart")
        super.onStart()

        loadPref()
        logState()

        if (_enabled) {
            display()
            schedule()
        }
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()
        if (isScheduled()) {
            unschedule()
        }
    }

    private fun display() {
        Log.d(tag, "display")
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val times = LocalDateTime.now().format(formatter).split(':')
        Log.d(tag, times[0]+':'+times[1])
        _binding?.clock?.text = getString(R.string.clock_hh_mm, times[0], times[1])
    }

    private fun logState() {
        Log.d(tag, "state={")
        Log.d(tag, "  _enabled=" + _enabled.toString())
        Log.d(tag, "}")
    }

    private fun isScheduled(): Boolean {
        return true == _runnable?.let { _handler.hasCallbacks(it) }
    }

    private fun schedule() {
        Log.d(tag, "schedule")
        // remaining ms time until next minute (10ms of safety)
        val remainingMs = 60_010 - System.currentTimeMillis() % 60_000
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