package tibarj.tranquilstopwatch

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tibarj.tranquilstopwatch.databinding.AboutFragmentBinding
import kotlin.math.floor


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AboutFragment : Fragment() {
    private val tag: String = "AboutFragment"
    private var _binding: AboutFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView")
        _binding = AboutFragmentBinding.inflate(inflater, container, false)

        val runtime = requireContext().getSharedPreferences(
            "StopwatchFragment",
            Context.MODE_PRIVATE
        ).getLong("runtime", 0L)
        // total does not track the ongoing runtime
        // ie the stopwatch must be reset for its ongoing value to be taken into account
        val hours = floor(runtime / 3600000.0).toInt()

        _binding?.textviewRuntime?.text = resources.getQuantityString(
            R.plurals.about_runtime,
            hours,
            hours
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}