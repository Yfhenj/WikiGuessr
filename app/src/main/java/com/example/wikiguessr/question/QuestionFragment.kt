package com.example.wikiguessr.question

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.wikiguessr.databinding.QuestionFragmentBinding
import com.google.android.material.snackbar.Snackbar

class QuestionFragment : Fragment() {

    private lateinit var binding: QuestionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,

        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = QuestionFragmentBinding.inflate(inflater)

        val activity = requireNotNull(this.activity)

        val questionViewModel = ViewModelProviders
            .of(this, QuestionViewModelFactory(activity.application))
            .get(QuestionViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = questionViewModel

//        questionViewModel.repository.preloadedQuestions.observe(this, Observer {
//            if (it.size <= 10 || it == null) {
//                questionViewModel.getWikiPage()
//                Log.i("MyTags", "${it.size}")
//            }
//        })

        questionViewModel.noInternetSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it && questionViewModel.repository.preloadedQuestions.value?.size == 0) {
                Snackbar.make(this.view!!, "No network conection", Snackbar.LENGTH_LONG).show()
                questionViewModel.noInternetSnackbarEventDone()
            }
        })

        return binding.root
    }
}