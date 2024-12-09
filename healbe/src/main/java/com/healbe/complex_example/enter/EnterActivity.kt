package com.healbe.complex_example.enter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.healbe.complex_example.SplashActivity
import com.healbe.complex_example.connect.ConnectActivity
import com.healbe.complex_example.tools.BaseActivity
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.user.data.HealbeSessionState

class EnterActivity : BaseActivity(), EnterRouter {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSdkInitialized) return

        if (savedInstanceState == null) {
            when (HealbeSdk.get().USER.sessionState) {
                HealbeSessionState.USER_NOT_AUTHORIZED -> goLogin()
                HealbeSessionState.NEED_TO_FILL_PERSONAL, HealbeSessionState.NEED_TO_FILL_PARAMS -> goFillData()
                HealbeSessionState.VALID_NEW_USER, HealbeSessionState.VALID_OLD_USER -> goConnect()
                HealbeSessionState.SESSION_NOT_PREPARED -> goSplash()
            }
        }
    }

    override fun goSplash() {
        goToActivity(SplashActivity::class.java)
    }

    override fun goConnect() {
        goToActivity(ConnectActivity::class.java)
    }

    private fun goToActivity(activityClass: Class<out ComponentActivity>) {
        val bundle = createAnimationBundle(this)
        startActivity(Intent(this, activityClass), bundle)
        finish()
    }

    override fun goLogin() {
        replaceWithFragment(LoginFragment.newInstance())
    }

    override fun goSignUp() {
        replaceWithFragment(SignUpFragment.newInstance())
    }

    override fun goFillData() {
        replaceWithFragment(FillDataFragment.newInstance())
    }

    private fun replaceWithFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply { replace(android.R.id.content, fragment) }
            .commit()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(
                Intent(context, EnterActivity::class.java),
                createAnimationBundle(context)
            )
        }

        private fun createAnimationBundle(context: Context) = ActivityOptionsCompat
            .makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
            .toBundle()
    }
}