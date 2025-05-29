package com.example.civic_trackapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
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
    private val args: IssueDetailFragmentArgs by navArgs()
    private val viewModel: IssueDetailViewModel by viewModels()

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

        val issueId = args.issueId
        viewModel.loadIssueDetails(issueId)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}