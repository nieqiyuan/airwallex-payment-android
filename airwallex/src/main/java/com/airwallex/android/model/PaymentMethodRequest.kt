package com.airwallex.android.model

import android.os.Parcelable
import com.airwallex.android.CurrencyUtils
import com.airwallex.android.model.parser.PaymentMethodParser
import kotlinx.parcelize.Parcelize

@Parcelize
class PaymentMethodRequest(

    /**
     * Unique identifier for the payment method
     */
    val id: String? = null,

    /**
     * Type of the payment method
     */
    val type: PaymentMethodType,

    /**
     * Redirect Request
     */
    val redirectRequest: AirwallexPaymentRequest? = null,

    /**
     * Card information for the payment method
     */
    val card: PaymentMethod.Card? = null,

    /**
     * Billing information for the payment method
     */
    val billing: Billing? = null

) : AirwallexRequestModel, Parcelable {

    override fun toParamMap(): Map<String, Any> {
        return mapOf<String, Any>()
            .plus(
                id?.let {
                    mapOf(PaymentMethodParser.FIELD_ID to it)
                }.orEmpty()
            )
            .plus(
                mapOf(PaymentMethodParser.FIELD_TYPE to type.value)
            )
            .plus(
                redirectRequest?.let {
                    mapOf(type.value to it.toParamMap())
                }.orEmpty()
            )
            .plus(
                card?.let {
                    mapOf(PaymentMethodParser.FIELD_CARD to it.toParamMap())
                }.orEmpty()
            )
            .plus(
                billing?.let {
                    mapOf(PaymentMethodParser.FIELD_BILLING to it.toParamMap())
                }.orEmpty()
            )
    }

    class Builder(
        val type: PaymentMethodType
    ) : ObjectBuilder<PaymentMethodRequest> {
        private var redirectRequest: AirwallexPaymentRequest? = null

        private var card: PaymentMethod.Card? = null

        private var billing: Billing? = null

        fun setThirdPartyPaymentMethodRequest(
            name: String? = null,
            email: String? = null,
            phone: String? = null,
            currency: String? = null,
            bank: Bank? = null
        ): Builder = apply {
            if (type != PaymentMethodType.CARD) {
                redirectRequest = AirwallexPaymentRequest(
                    bank = bank,
                    name = name,
                    email = email,
                    phone = phone,
                    countryCode = currency?.let {
                        CurrencyUtils.currencyToCountryMap[currency]
                    }
                )
            }
        }

        fun setCardPaymentMethodRequest(
            card: PaymentMethod.Card?,
            billing: Billing?
        ): Builder = apply {
            this.card = card
            this.billing = billing
        }

        override fun build(): PaymentMethodRequest {
            return PaymentMethodRequest(
                type = type,
                redirectRequest = redirectRequest,
                card = card,
                billing = billing
            )
        }
    }
}
