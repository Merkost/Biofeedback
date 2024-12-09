package com.healbe.complex_example.connect

// small router for fragment navigation
interface ConnectRouter {
    fun goState(state: State, back: Boolean)
    fun logout()
    fun connected()

    enum class State {
        PREPARING, CONNECT, SEARCH, ERROR, ENTER_PIN, SETUP_PIN
    }
}