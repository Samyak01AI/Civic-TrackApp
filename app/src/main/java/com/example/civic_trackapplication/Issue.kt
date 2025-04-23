package com.example.civic_trackapplication

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class Issue : Fragment() {

    companion object {
        fun newInstance() = Issue()
    }

    private val viewModel: IssueViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.filteredIssues.observe(viewLifecycleOwner) { issues ->
            // Update your RecyclerView or UI with the filtered issues
            /*issuesAdapter.submitList(issues)*/
        }

        // Trigger a fetch
        viewModel.fetchIssuesByUser(Firebase.auth.currentUser?.uid ?: "anonymous")

    }

   /* override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_issue, container, false)
    }*/
}