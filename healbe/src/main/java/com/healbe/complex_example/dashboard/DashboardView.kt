package com.healbe.complex_example.dashboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.healbe.complex_example.R
import com.healbe.complex_example.databinding.DashViewBinding

@Suppress("unused")
class DashboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: DashViewBinding =
        DashViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val params = context.obtainStyledAttributes(attrs, R.styleable.DashboardView)
        try {
            // Icon params
            val iconId = params.getResourceId(R.styleable.DashboardView_icon, -1)
            if (iconId != -1)
                binding.icon.setImageResource(iconId)

            val defCol = ContextCompat.getColor(context, R.color.light_black_38)
            val iconCol = params.getColor(R.styleable.DashboardView_round_color, defCol)
            binding.round.setCardBackgroundColor(iconCol)

            // progress params
            binding.progress.isVisible =
                params.getBoolean(R.styleable.DashboardView_progress_visible, false)

            //header params
            binding.title.text = params.getString(R.styleable.DashboardView_title_string)

            //center text params
            with (binding.text) {
                text = params.getString(R.styleable.DashboardView_text_string)
                isVisible = params.getBoolean(R.styleable.DashboardView_text_visible, true)
            }

            //bottom text params
            with (binding.value) {
                text = params.getString(R.styleable.DashboardView_value_string)

                isVisible = params.getBoolean(R.styleable.DashboardView_value_visible, true)
            }
        } finally {
            params.recycle()
        }
    }

    fun setIconBackgroundColor(@ColorInt color: Int) {
        binding.round.setCardBackgroundColor(color)
    }

    fun setMainIconResource(@ColorRes res: Int) {
        binding.icon.setImageResource(res)
    }

    fun setProgressVisible(b: Boolean) {
        binding.progress.visibility = if (b) VISIBLE else GONE
    }

    fun setProgressIndeterminate() {
        binding.progress.isIndeterminate = true
    }

    fun setProgressNonIndeterminate() {
        binding.progress.isIndeterminate = false
    }

    fun setProgress(progress: Int) {
        binding.progress.progress = progress
    }

    fun setTitleText(@StringRes res: Int) {
        binding.title.setText(res)
    }

    fun setTitleText(s: CharSequence?) {
        binding.title.text = s
    }

    fun setText(@StringRes res: Int) {
        with (binding.text) {
            setText(res)
            requestLayout()
        }
    }

    fun setText(s: CharSequence?) {
        binding.text.text = s
    }

    fun setTextVisible(b: Boolean) {
        binding.text.isVisible = b
    }

    fun setValueText(@StringRes res: Int) {
        binding.value.setText(res)
    }

    fun setValueText(s: CharSequence?) {
        binding.value.text = s
    }

    fun setValueVisible(b: Boolean) {
        binding.value.isVisible = b
    }
}