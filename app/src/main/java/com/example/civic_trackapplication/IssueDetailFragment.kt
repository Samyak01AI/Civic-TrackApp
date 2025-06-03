package com.example.civic_trackapplication

import NetworkMonitor
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.example.civic_trackapplication.databinding.FragmentIssueDetailBinding
import com.example.civic_trackapplication.viewmodels.IssueDetailViewModel
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IssueDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IssueDetailFragment : Fragment() {
    private var _binding: FragmentIssueDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IssueDetailViewModel by viewModels()

    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIssueDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.issue.observe(viewLifecycleOwner) { issue ->
            issue?.let { updateUI(it) }
        }
    }

    private fun updateUI(issue: Issue) {
        binding.apply {
            tvTitle.text = issue.title
            tvStatus.text = issue.status
            tvLocation.text = issue.location.toString()

            Glide.with(this@IssueDetailFragment)
                .load(issue.imageUrl)
                .into(ivIssueImage)
        }
    }

    private var connectionDialog: Dialog? = null

    fun showNoConnectionDialog(context: Context) {
        if (connectionDialog?.isShowing == true) return // Already shown

        connectionDialog = Dialog(context).apply {
            setContentView(R.layout.popup_connection_status)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(false) // Prevent manual dismissal

            val lottieView = findViewById<LottieAnimationView>(R.id.lottieStatus)
            lottieView.setAnimation("disconnected.json")
            lottieView.playAnimation()

            show()
        }
    }

    fun dismissConnectionDialog() {
        connectionDialog?.dismiss()
        connectionDialog = null
    }
    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onStart() {
        super.onStart()
        networkMonitor = NetworkMonitor(requireContext())

        networkMonitor.startMonitoring()

        // Observe connectivity changes
        networkMonitor.isConnected.observe(this) { isConnected ->
            if (isConnected) {
                dismissConnectionDialog()
            } else {
                showNoConnectionDialog(requireContext())
            }
        }

        if (!isInternetAvailable(requireContext())) {
            showNoConnectionDialog(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}