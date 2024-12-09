package com.healbe.complex_example.enter

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.StringRes
import androidx.core.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.healbe.complex_example.R
import com.healbe.complex_example.databinding.FragmentFillDataBinding
import com.healbe.complex_example.tools.BirthDateDateValidator
import com.healbe.complex_example.tools.BirthDateDateValidator.defaultOpenAt
import com.healbe.complex_example.tools.BirthDateDateValidator.endConstraint
import com.healbe.complex_example.tools.BirthDateDateValidator.startConstraint
import com.healbe.complex_example.tools.TextWatcherHelper.onTextChanged
import com.healbe.examples.common.getCurrentLocale
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.converters.DateConverter
import com.healbe.healbesdk.business_api.tools.ValidatorTool.HEIGHT_BOTTOM
import com.healbe.healbesdk.business_api.tools.ValidatorTool.HEIGHT_TOP
import com.healbe.healbesdk.business_api.tools.ValidatorTool.ISO_CODES
import com.healbe.healbesdk.business_api.tools.ValidatorTool.WEIGHT_BOTTOM
import com.healbe.healbesdk.business_api.tools.ValidatorTool.WEIGHT_TOP
import com.healbe.healbesdk.business_api.tools.ValidatorTool.isCountryISOCode
import com.healbe.healbesdk.business_api.tools.ValidatorTool.isValidBirthDate
import com.healbe.healbesdk.business_api.user.data.*
import com.healbe.healbesdk.business_api.user.data.HealbeUser.Companion.SEX_FEMALE
import com.healbe.healbesdk.business_api.user.data.HealbeUser.Companion.SEX_MALE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*

class FillDataFragment : Fragment() {
    private val destroy = CompositeDisposable()

    private var router: EnterRouter? = null
    private var binding: FragmentFillDataBinding? = null

    private var firstNameTextWatcher: TextWatcher? = null
    private var lastNameTextWatcher: TextWatcher? = null
    private var sexTextWatcher: TextWatcher? = null
    private var countryTextWatcher: TextWatcher? = null
    private var weightTextWatcher: TextWatcher? = null
    private var heightTextWatcher: TextWatcher? = null

    private var user: HealbeUser? = null
    private var male: String? = null
    private var female: String? = null
    private var countriesMap: Map<String, String>? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var sex: Int? = null
    private var birthDate: YMD? = null
    private var isoCode: String? = null
    private var weight: Double? = null
    private var height: Double? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        router = context as? EnterRouter
            ?: throw RuntimeException("$context must implement EnterRouter")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentFillDataBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            // for simplicity we do it in the main thread
            val user = HealbeSdk.get().USER.user.blockingGet()
                .also { user = it }
            showSavingProgress(false)
            bind()
            initFields(this, user, savedInstanceState)

            val headerTopMargin = header.marginTop
            val signOutBottomMargin = signOut.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                binding?.apply {
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                        header.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            topMargin = headerTopMargin + insets.top
                        }
                        signOut.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = signOutBottomMargin + insets.bottom
                        }
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun FragmentFillDataBinding.bind() {
        val context = root.context

        button.setOnClickListener { onButtonClick() }
        signOut.setOnClickListener { onSignOutClick() }

        val male = context.getString(R.string.male)
            .also { this@FillDataFragment.male = it }
        val female = context.getString(R.string.female)
            .also { this@FillDataFragment.female = it }
        sex.setAdapter(ArrayAdapter(context, R.layout.spinner_item, arrayOf(male, female)))

        birthDate.setOnClickListener { onBirthDateClick() }

        val defaultLanguage = context.getCurrentLocale().language
        val map = buildMap<String, String>(ISO_CODES.size) {
            var displayCountry: String
            for (isoCode in ISO_CODES) {
                if (isoCode.isEmpty())
                    continue

                displayCountry = Locale(defaultLanguage, isoCode).displayCountry
                if (!isoCode.equals(displayCountry, ignoreCase = true)) {
                    put(displayCountry, isoCode)
                }
            }
        }
        val countries = map.keys.toTypedArray().also { it.sort() }
        countriesMap = map
        country.setAdapter(ArrayAdapter(context, R.layout.spinner_item, countries))

        firstName.addTextChangedListener(
            onTextChanged { firstName: String -> validateAndUpdateFirstName(firstName) }
                .also { firstNameTextWatcher = it }
        )
        lastName.addTextChangedListener(
            onTextChanged { lastName: String -> validateAndUpdateLastName(lastName) }
                .also { lastNameTextWatcher = it }
        )
        sex.addTextChangedListener(
            onTextChanged { sex: String -> updateSex(sex) }
                .also { sexTextWatcher = it }
        )
        country.addTextChangedListener(
            onTextChanged { isoCode: String -> updateCountry(isoCode) }
                .also { countryTextWatcher = it }
        )
        weight.addTextChangedListener(
            onTextChanged { weightStr: String -> validateAndUpdateWeight(weightStr) }
                .also { weightTextWatcher = it }
        )
        height.addTextChangedListener(
            onTextChanged { heightStr: String -> validateAndUpdateHeight(heightStr) }
                .also { weightTextWatcher = it }
        )
    }

    private fun initFields(
        binding: FragmentFillDataBinding,
        user: HealbeUser,
        savedInstanceState: Bundle?
    ) {
        val context = binding.root.context

        if (savedInstanceState != null) {
            firstName = savedInstanceState.getString(ARG_FIRST_NAME)
        }
        if (firstName == null) {
            firstName = user.userName.firstName
            if (firstName?.isBlank() == true) {
                firstName = null
            }
        }
        binding.firstName.setText(firstName)

        if (savedInstanceState != null) {
            lastName = savedInstanceState.getString(ARG_LAST_NAME)
        }
        if (lastName == null) {
            lastName = user.userName.lastName
            if (lastName?.isBlank() == true) {
                lastName = null
            }
        }
        binding.lastName.setText(lastName)

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_SEX)) {
            sex = savedInstanceState.getInt(ARG_SEX)
        }
        if (sex == null) {
            val sex = user.sex
            this.sex = if (HealbeUser.isValidSex(sex)) sex else null
        }
        if (sex == null) {
            binding.sex.clearListSelection()
        } else {
            binding.sex.setText(if (sex == SEX_MALE) male else female, false)
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_BIRTH_DATE)) {
            val millis = savedInstanceState.getLong(ARG_BIRTH_DATE)
            val date = Date(millis)
            birthDate = DateConverter.dateToYMD(date)
        }
        if (birthDate == null && !user.birthDate.isEmpty && isValidBirthDate(user.birthDate)) {
            birthDate = user.birthDate
        }
        binding.birthDate.setText(
            birthDate?.let {
                if (isValidBirthDate(it)) {
                    val date = DateConverter.ymdToTime(birthDate)
                    DateUtils.formatDateTime(context, date.time, DateUtils.FORMAT_NUMERIC_DATE)
                } else {
                    null
                }
            }
        )

        if (savedInstanceState != null) {
            isoCode = savedInstanceState.getString(ARG_ISO_CODE)
        }
        if (isoCode == null) {
            val isoCode = user.address.countryISOCode
            this.isoCode = if (isoCode != null && isCountryISOCode(isoCode)) isoCode else null
        }
        val selectedCountry = countriesMap?.firstNotNullOfOrNull { (key, value) ->
            if (value.equals(isoCode, ignoreCase = true)) key else null
        }
        if (selectedCountry != null) {
            binding.country.setText(selectedCountry, false)
        } else {
            binding.country.clearListSelection()
        }

        if (savedInstanceState != null) {
            weight = savedInstanceState.getDouble(ARG_WEIGHT)
        }
        if (weight == null) {
            val weight = user.bodyMeasurements.weightKG
            this.weight = if (BodyMeasurements.validateWeight(weight)) weight.toDouble() else null
        }
        // in real program weight can be converted into other units than kilograms. See WeightUnits
        binding.weight.setText(weight?.toString())

        if (savedInstanceState != null) {
            height = savedInstanceState.getDouble(ARG_HEIGHT)
        }
        if (height == null) {
            val height = user.bodyMeasurements.heightCM
            this.height = if (BodyMeasurements.validateHeight(height)) height.toDouble() else null
        }
        // in real program height can be converted into other units than centimeters. See HeightUnits
        binding.height.setText(height?.toString())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        fun Bundle.putStringIfNotNull(value: String?, key: String) {
            if (value != null) {
                putString(key, value)
            }
        }

        fun Bundle.putDoubleIfNotNull(value: Double?, key: String) {
            if (value != null) {
                putDouble(key, value)
            }
        }

        with(outState) {
            putStringIfNotNull(firstName, ARG_FIRST_NAME)
            putStringIfNotNull(lastName, ARG_LAST_NAME)
            putStringIfNotNull(isoCode, ARG_ISO_CODE)
            putDoubleIfNotNull(weight, ARG_WEIGHT)
            putDoubleIfNotNull(height, ARG_HEIGHT)
            sex?.let {
                putInt(ARG_SEX, it)
            }
            birthDate?.let {
                val date = DateConverter.ymdToTime(it)
                val millis = date.time
                putLong(ARG_BIRTH_DATE, millis)
            }
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            unbind()
            binding = null
        }
        super.onDestroyView()
    }

    private fun FragmentFillDataBinding.unbind() {
        button.setOnClickListener(null)
        signOut.setOnClickListener(null)
        sex.setAdapter(null)
        birthDate.setOnClickListener(null)
        country.setAdapter(null)
        firstNameTextWatcher?.let {
            firstName.removeTextChangedListener(it)
            firstNameTextWatcher = null
        }
        lastNameTextWatcher?.let {
            lastName.removeTextChangedListener(it)
            lastNameTextWatcher = null
        }
        sexTextWatcher?.let {
            sex.removeTextChangedListener(it)
            sexTextWatcher = null
        }
        countryTextWatcher?.let {
            country.removeTextChangedListener(it)
            countryTextWatcher = null
        }
        weightTextWatcher?.let {
            weight.removeTextChangedListener(it)
            weightTextWatcher = null
        }
        heightTextWatcher?.let {
            height.removeTextChangedListener(it)
            heightTextWatcher = null
        }
    }

    override fun onDetach() {
        router = null
        super.onDetach()
    }

    private fun onButtonClick() {
        val binding = binding ?: return
        val user = user ?: return

        if (binding.isAllDataValid()) {
            user.userName.firstName = firstName!!
            user.userName.lastName = lastName!!
            user.sex = sex!!
            user.birthDate = birthDate!!
            user.address.setCountryISOCode(isoCode!!)
            user.bodyMeasurements.weightKG = weight!!.toFloat()
            user.bodyMeasurements.heightCM = height!!.toFloat()
        } else {
            return
        }

        destroy.add(
            HealbeSdk.get().USER.updateUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { binding.showSavingProgress(true) }
                .subscribe(
                    { state: HealbeSessionState ->
                        when (state) {
                            HealbeSessionState.USER_NOT_AUTHORIZED -> {
                                binding.root.showSnackbar(R.string.unknown_error)
                                router?.goLogin()
                            }

                            HealbeSessionState.NEED_TO_FILL_PERSONAL,
                            HealbeSessionState.NEED_TO_FILL_PARAMS ->
                                binding.root.showSnackbar(R.string.unknown_error)

                            HealbeSessionState.VALID_NEW_USER, HealbeSessionState.VALID_OLD_USER ->
                                router?.goConnect()

                            HealbeSessionState.SESSION_NOT_PREPARED -> router?.goConnect()
                        }
                    },
                    { throwable ->
                        Timber.e(throwable)
                        binding.showSavingProgress(false)
                    }
                )
        )
    }

    private fun FragmentFillDataBinding.showSavingProgress(inProgress: Boolean) {
        button.visibility = if (inProgress) View.INVISIBLE else View.VISIBLE
        signOut.isEnabled = !inProgress
        progress.visibility = if (inProgress) View.VISIBLE else View.INVISIBLE
    }

    private fun onBirthDateClick() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setStart(startConstraint)
            .setEnd(endConstraint)
            .setValidator(BirthDateDateValidator)
            .setOpenAt(defaultOpenAt)
            .build()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.date_of_birth)
            .setCalendarConstraints(calendarConstraints)
            .build()
        datePicker.addOnPositiveButtonClickListener { selection: Long? ->
            if (selection == null) return@addOnPositiveButtonClickListener

            val date = Date(selection)
            this.birthDate = DateConverter.dateToYMD(date)
            binding?.apply {
                birthDate.setText(
                    DateUtils.formatDateTime(root.context, date.time, DateUtils.FORMAT_NUMERIC_DATE)
                )
            }
        }
        if (isAdded) {
            datePicker.show(parentFragmentManager, "birth_date_picker")
        }
    }

    private fun onSignOutClick() {
        destroy.add(
            HealbeSdk.get().USER.logout()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    binding?.apply {
                        button.isEnabled = false
                        signOut.isEnabled = false
                    }
                }
                .subscribe(
                    { goLogin() },
                    { t: Throwable ->
                        Timber.e(t, "Error during sign out: %s", t.message)
                        goLogin()
                    }
                )
        )
    }

    private fun goLogin() {
        router?.goLogin()
    }

    private fun validateAndUpdateFirstName(firstName: String) {
        this.firstName = firstName
        binding?.apply {
            if (firstName.isEmpty() || UserName.validateName(firstName)) {
                firstNameLayout.isErrorEnabled = false
            } else {
                firstNameLayout.error = root.context.getString(R.string.name_error)
            }
        }
    }

    private fun validateAndUpdateLastName(lastName: String) {
        this.lastName = lastName
        binding?.apply {
            if (lastName.isEmpty() || UserName.validateName(lastName)) {
                lastNameLayout.isErrorEnabled = false
            } else {
                lastNameLayout.error = root.context.getString(R.string.name_error)
            }
        }
    }

    private fun updateSex(sex: String) {
        binding?.sexLayout?.isErrorEnabled = false
        this.sex = if (sex.isEmpty()) {
            null
        } else {
            if (sex == male) SEX_MALE else SEX_FEMALE
        }
    }

    private fun updateCountry(isoCode: String) {
        binding?.countryLayout?.isErrorEnabled = false
        this.isoCode = isoCode
    }

    private fun validateAndUpdateWeight(weightStr: String) {
        if (weightStr.trim().let { it.isEmpty() || it == "." }) {
            binding?.weightLayout?.isErrorEnabled = false
            weight = null
        } else {
            val weight = weightStr.toDoubleOrNull()
            this.weight = weight
            weight?.toFloat()?.let {
                if (BodyMeasurements.validateWeight(weight.toFloat())) {
                    user?.bodyMeasurements?.weightKG = weight.toFloat()
                }
            }
            binding?.apply {
                if (weight == null || !BodyMeasurements.validateWeight(weight.toFloat())) {
                    showInvalidWeightError()
                } else {
                    weightLayout.isErrorEnabled = false
                }
            }
        }
    }

    private fun validateAndUpdateHeight(heightStr: String) {
        if (heightStr.trim().let { it.isEmpty() || it == "." }) {
            binding?.heightLayout?.isErrorEnabled = false
            height = null
        } else {
            val height: Double? = try {
                java.lang.Double.valueOf(heightStr)
            } catch (e: NumberFormatException) {
                null
            }
            this.height = height
            binding?.apply {
                if (height == null || !BodyMeasurements.validateHeight(height.toFloat())) {
                    showInvalidHeightError()
                } else {
                    heightLayout.isErrorEnabled = false
                }
            }
        }
    }

    private fun FragmentFillDataBinding.isAllDataValid(): Boolean {
        val context = root.context
        val emptyFieldError = context.getString(R.string.field_is_empty)
        var result = true

        if (!firstNameLayout.isErrorEnabled && firstName.text.isNullOrBlank()) {
            firstNameLayout.error = emptyFieldError
        }
        result = result && !firstNameLayout.isErrorEnabled

        if (!lastNameLayout.isErrorEnabled && lastName.text.isNullOrBlank()) {
            lastNameLayout.error = emptyFieldError
        }
        result = result && !lastNameLayout.isErrorEnabled

        if (sex.text.isNullOrBlank()) {
            sexLayout.error = emptyFieldError
        }
        result = result && !sexLayout.isErrorEnabled

        if (birthDate.text.isNullOrBlank()) {
            birthDateLayout.error = emptyFieldError
        }
        result = result && !birthDateLayout.isErrorEnabled

        if (country.text.isNullOrBlank()) {
            countryLayout.error = emptyFieldError
        }
        result = result && !countryLayout.isErrorEnabled

        if (!weightLayout.isErrorEnabled) {
            if (weight.text.let { it.isNullOrBlank() || it.trim() == "." }) {
                weightLayout.error = emptyFieldError
            } else if (this@FillDataFragment.weight == null) {
                showInvalidWeightError()
            }
        }
        result = result && !weightLayout.isErrorEnabled

        if (!heightLayout.isErrorEnabled) {
            if (height.text.let { it.isNullOrBlank() || it.trim() == "." }) {
                heightLayout.error = emptyFieldError
            } else if (this@FillDataFragment.height == null) {
                showInvalidHeightError()
            }
        }
        result = result && !heightLayout.isErrorEnabled

        return result
    }

    private fun FragmentFillDataBinding.showInvalidWeightError() {
        weightLayout.error =
            root.context.getString(R.string.weight_error, WEIGHT_BOTTOM.toInt(), WEIGHT_TOP.toInt())
    }

    private fun FragmentFillDataBinding.showInvalidHeightError() {
        heightLayout.error =
            root.context.getString(R.string.height_error, HEIGHT_BOTTOM, HEIGHT_TOP)
    }

    private fun View.showSnackbar(@StringRes message: Int) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_FIRST_NAME = "first_name"
        private const val ARG_LAST_NAME = "last_name"
        private const val ARG_SEX = "sex"
        private const val ARG_BIRTH_DATE = "birth_date"
        private const val ARG_ISO_CODE = "iso_code"
        private const val ARG_WEIGHT = "weight"
        private const val ARG_HEIGHT = "height"

        fun newInstance(): FillDataFragment = FillDataFragment()
    }
}