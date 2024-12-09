package com.healbe.complex_example.connect.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.*
import com.healbe.complex_example.connect.ConnectRouter
import com.healbe.complex_example.connect.ConnectionRoutedFragment
import com.healbe.complex_example.databinding.FragmentErrorBinding
import com.healbe.healbesdk.business_api.HealbeSdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class ErrorFragment : ConnectionRoutedFragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentErrorBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goSearch()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentErrorBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            skipButton.setOnClickListener { goSearch() }

            val headerTopMargin = header.marginTop
            val skipButtonBottomMargin = skipButton.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                    binding?.apply {
                        header.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            topMargin = headerTopMargin + insets.top
                        }
                        skipButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = skipButtonBottomMargin + insets.bottom
                        }
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            skipButton.setOnClickListener(null)
            binding = null
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    private fun goSearch() {
        destroy.add(
            HealbeSdk.get().GOBE.disconnect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { router?.goState(ConnectRouter.State.SEARCH, true) },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    companion object {
        fun newInstance(): ErrorFragment = ErrorFragment()
    }
}