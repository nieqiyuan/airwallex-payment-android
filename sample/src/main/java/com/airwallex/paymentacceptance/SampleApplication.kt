package com.airwallex.paymentacceptance

import android.app.Application
import com.airwallex.android.Airwallex
import com.airwallex.android.AirwallexConfiguration
import com.airwallex.android.Environment

class SampleApplication : Application() {

    companion object {
        lateinit var instance: SampleApplication
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        Airwallex.initialize(
            AirwallexConfiguration.Builder(this)
                .setEnvironment(Environment.STAGING)
                .enableLogging(true)
                .build()
        )
    }
}