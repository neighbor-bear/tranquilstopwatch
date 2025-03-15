package tibarj.tranquilstopwatch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
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
    private val _handlerBtn = Handler(Looper.getMainLooper())
    private var _runnableMvt: Runnable? = null
    private val _handlerMvt = Handler(Looper.getMainLooper())
    private var _movement: Int = 0

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

        _binding.aboutBtn.setOnClickListener { view ->
            startActivity(Intent(this, AboutActivity::class.java))
        }
        _binding.settingsBtn.setOnClickListener { view ->
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        _runnableBtn = Runnable {
            _binding.settingsBtn.hide()
            _binding.aboutBtn.hide()
        }
        _runnableMvt = Runnable {
            onMvtTimerTick()
        }
        showSettingsButton()
        initTapListeners()
    }

    // visible but not interactable
    override fun onStart() {
        Log.d(tag, "onStart")
        super.onStart()

        loadPref()
        logState()

        if (0 != _movement) {
            scheduleMvt()
        }
    }

    override fun onStop() {
        Log.d(tag, "onStop")
        super.onStop()
        if (isMvtScheduled()) {
            unscheduleMvt()
        }
    }

    fun showSettingsButton() {
        Log.d(tag, " showSettingsButton")
        _runnableBtn?.let {
            _handlerBtn.removeCallbacks(it)
        }
        _binding.settingsBtn.show()
        _binding.aboutBtn.show()
        _handlerBtn.postDelayed(_runnableBtn!!, 5000)
    }

    private fun isMvtScheduled(): Boolean {
        return true == _runnableMvt?.let { _handlerMvt.hasCallbacks(it) }
    }

    fun scheduleMvt() {
        Log.d(tag, " >mvt scheduled in 60s")
        _handlerMvt.postDelayed(_runnableMvt!!, 60000)
    }

    private fun unscheduleMvt() {
        Log.d(tag, "unscheduleMvt")
        _runnableMvt?.let {
            _handlerMvt.removeCallbacks(it)
        }
    }

    fun showClock(show: Boolean) {
        _binding.contentMain.fragmentClock.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun loadPref() {
        Log.d(tag, "applyPref")
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        val movement = pref.getInt(
            getString(R.string.global_movement_key),
            resources.getInteger(R.integer.default_global_movement)
        )
        if (_movement != movement) {
            Log.d(tag, "setMovement " + movement.toString())
            _movement = movement
            if (0 == _movement) {
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
        _binding.contentMain.spaceTop.setOnClickListener {
            Log.d(tag, "OnClickPanel")
            showSettingsButton()
        }
        _binding.contentMain.spaceBottom.setOnClickListener {
            Log.d(tag, "OnClickPanel")
            showSettingsButton()
        }
    }

    private fun changeMargins() {
        Log.d(tag, "changeMargins")
        val limit = 10 * _movement
        setMargins(Random.nextInt(-limit, limit + 1), Random.nextInt(-limit, limit + 1))
    }

    private fun setMargins(h: Int, v: Int) {
        Log.d(tag, "setMargins")
        val layoutParams = (_binding?.contentMain?.mainContent?.layoutParams as? MarginLayoutParams)
        layoutParams?.setMargins(h, v, -h, -v)
        _binding?.contentMain?.mainContent?.layoutParams = layoutParams
    }

    private fun logState() {
        Log.d(tag, "state={")
        Log.d(tag, "  _movement=" + _movement.toString())
        Log.d(tag, "}")
    }
}