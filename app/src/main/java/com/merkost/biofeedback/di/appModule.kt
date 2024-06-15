package com.merkost.biofeedback.di

import com.merkost.biofeedback.data.HeartRateRepository
import com.merkost.biofeedback.viewmodel.VisualizationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HeartRateRepository() }
    viewModel { VisualizationViewModel(get()) }
}