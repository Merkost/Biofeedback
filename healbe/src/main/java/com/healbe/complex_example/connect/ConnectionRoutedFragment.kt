package com.healbe.complex_example.connect

import android.content.Context
import androidx.fragment.app.Fragment

abstract class ConnectionRoutedFragment : Fragment() {
    var router: ConnectRouter? = null
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)
        router = if (context is ConnectRouter) {
            context
        } else {
            throw RuntimeException("$context must implement ConnectRouter")
        }
    }

    override fun onDetach() {
        router = null
        super.onDetach()
    }
}