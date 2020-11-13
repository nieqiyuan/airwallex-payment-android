package com.airwallex.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ClientSecret internal constructor(

    val value: String,

    val expiredTime: Date
) : AirwallexModel, Parcelable
