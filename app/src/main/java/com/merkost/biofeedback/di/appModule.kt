package com.merkost.biofeedback.di

import com.merkost.biofeedback.data.HealBeRepository
import com.merkost.biofeedback.data.HealbeExceptionHandler
import com.merkost.biofeedback.data.HeartRateRepository
import com.merkost.biofeedback.viewmodel.VisualizationViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HealbeExceptionHandler }

    single { HealBeRepository(androidApplication(), get()) }
    single { HeartRateRepository() }

    viewModel { VisualizationViewModel(get(), get()) }
}