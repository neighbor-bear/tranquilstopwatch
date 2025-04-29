package tibarj.tranquilstopwatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import tibarj.tranquilstopwatch.databinding.MainActivityBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val tag: String = "MainActivity"
    private lateinit var _binding: MainActivityBinding
    private var _runnableBtn: Runnable? = null
    private var _runnableMvt: Runnable? = null
    private val _handlerBtn = Handler(Looper.getMainLooper())
    private val _handlerMvt = Handler(Looper.getMainLooper())
    private var _isMvtScheduled = false
    private var _displacement: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = MainActivityBinding.inflate(layoutInflater)

        // apply preferences even if the user hasn't visited the settings activity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, true)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // hide bottom bar
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            view.onApplyWindowInsets(windowInsets)
        }

        supportActionBar?.hide()
        setContentView(_binding.root)

        _binding.aboutBtn.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        _binding.settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        _runnableBtn = Runnable {
            _binding.settingsBtn.hide()
            _binding.aboutBtn.hide()
        }
        _runnableMvt = Runnable {
            onMvtTimerTick()
        }
        showButtons()
        initTapListeners()
    }

    // visible but not interactable
    override fun onStart() {
        Log.d(tag, "onStart")
        super.onStart()

        loadPref()
        logState()

        if (0 != _displacement) {
            scheduleMvt()
        }
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()
        if (_isMvtScheduled) {
            unscheduleMvt()
        }
    }

    fun keepScreenOn() {
        Log.d(tag, "keepScreenOn")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun unkeepScreenOn() {
        Log.d(tag, "unkeepScreenOn")
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun showButtons() {
        Log.d(tag, "showButtons")
        _runnableBtn?.let {
            _handlerBtn.removeCallbacks(it)
        }
        _binding.settingsBtn.show()
        _binding.aboutBtn.show()
        val delay = resources.getInteger(R.integer.global_buttons_delay_ms)
        _handlerBtn.postDelayed(_runnableBtn!!, delay.toLong())
    }

    private fun showClock(show: Boolean) {
        _binding.mainContent.fragmentClock.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun showStopwatch(show: Boolean) {
        _binding.mainContent.fragmentStopwatch.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun scheduleMvt() {
        val delay = resources.getInteger(R.integer.global_displacement_delay_ms)
        Log.d(tag, " >mvt scheduled in " + delay.toString() + "ms")
        _isMvtScheduled = true
        _handlerMvt.postDelayed(_runnableMvt!!, delay.toLong())
    }

    private fun unscheduleMvt() {
        Log.d(tag, "unscheduleMvt")
        _runnableMvt?.let {
            _handlerMvt.removeCallbacks(it)
            _isMvtScheduled = false
        }
    }

    private fun loadPref() {
        Log.d(tag, "applyPref")
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        val clockEnabled = pref.getBoolean(
            getString(R.string.clock_enabled_key),
            resources.getBoolean(R.bool.default_clock_enabled)
        )
        showClock(clockEnabled)

        val stopwatchEnabled = pref.getBoolean(
            getString(R.string.stopwatch_enabled_key),
            resources.getBoolean(R.bool.default_stopwatch_enabled)
        )
        showStopwatch(stopwatchEnabled)

        val displacement = pref.getInt(
            getString(R.string.global_displacement_key),
            resources.getInteger(R.integer.default_global_displacement)
        )
        if (_displacement != displacement) {
            Log.d(tag, "setDisplacement $displacement")
            _displacement = displacement
            if (0 == _displacement) {
                setMargins(0, 0)
            } else {
                changeMargins()
            }
        }
    }

    private fun onMvtTimerTick() {
        Log.d(tag, "onMvtTimerTick")
        if (null === _binding) {
            return
        }
        changeMargins()
        scheduleMvt()
    }

    private fun initTapListeners() {
        Log.d(tag, "setTapListeners")
        _binding.panel.setOnClickListener {
            Log.d(tag, "OnClickPanel")
            showButtons()
        }
    }

    private fun changeMargins() {
        Log.d(tag, "changeMargins")

        val panelWidth = _binding.panel.width
        val panelHeight = _binding.panel.height
        val contentWidth = _binding.mainContent.content.width
        val contentHeight = _binding.mainContent.content.height

        val hSpace = if (0 != contentWidth) panelWidth - contentWidth else 0
        val vSpace = if (0 != contentHeight) panelHeight - contentHeight else 0

        // y_max|y_min = +|- vSpace / 2 ??
        // x_max|x_min = +|- hSpace / 2 ??
        val ratio = _displacement.toDouble() / resources.getInteger(R.integer.global_displacement_max).toDouble()
        val hMax = (ratio * hSpace.toDouble()).toInt()
        val vMax = (ratio * vSpace.toDouble()).toInt()
        val left = Random.nextInt(-hMax, hMax + 1)
        val top = Random.nextInt(-vMax, vMax + 1)

        Log.d(tag, "panelWidth $panelWidth")
        Log.d(tag, "panelHeight $panelHeight")
        Log.d(tag, "contentWidth $contentWidth")
        Log.d(tag, "contentHeight $contentHeight")
        Log.d(tag, "hSpace $hSpace")
        Log.d(tag, "vSpace $vSpace")
        Log.d(tag, "ratio $ratio")
        Log.d(tag, "hMax $hMax")
        Log.d(tag, "vMax $vMax")
        Log.d(tag, "left $left")
        Log.d(tag, "top $top")
        setMargins(left, top)
    }

    private fun setMargins(h: Int, v: Int) {
        Log.d(tag, "setMargins=($h,$v)")
        val layoutParams = (_binding.mainContent.content.layoutParams as? MarginLayoutParams)
        layoutParams?.leftMargin = h
        layoutParams?.topMargin = v
        _binding.mainContent.content.layoutParams = layoutParams
    }

    private fun logState() {
        Log.d(tag, "state={")
        Log.d(tag, "  _displacement=$_displacement")
        Log.d(tag, "}")
    }
}