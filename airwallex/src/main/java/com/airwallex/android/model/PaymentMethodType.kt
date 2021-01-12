package com.airwallex.android.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * The type of [PaymentMethod]
 */
@Parcelize
enum class PaymentMethodType(val value: String) : Parcelable {

    VISA("visa"),
    MASTERCARD("mastercard"),
    ALIPAY_CN("alipaycn"),
    ALIPAY_HK("alipayhk"),
    WECHAT("wechatpay"),
    DANA("dana"),
    GCASH("gcash"),
    KAKAOPAY("kakaopay"),
    TNG("tng");

    internal companion object {
        internal fun fromValue(value: String?): PaymentMethodType? {
            return values().firstOrNull { it.value == value }
        }
    }
}
