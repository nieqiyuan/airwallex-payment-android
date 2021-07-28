package com.airwallex.android.model

/**
 * The params that used for create [PaymentConsent]
 */
data class CreatePaymentConsentParams constructor(
    val clientSecret: String,
    val customerId: String,

    /**
     * ID of the PaymentMethod attached for subsequent payments. Must be set when type is card.
     */
    val paymentMethodId: String? = null,

    /**
     * Type of the PaymentMethod. One of card, alipayhk, kakaopay, gcash, dana, tng
     */
    val paymentMethodType: PaymentMethodType,

    /**
     * The party to trigger subsequent payments. Can be one of merchant, customer. If type of payment_method is card, both merchant and customer is supported. Otherwise, only merchant is supported
     */
    val nextTriggeredBy: PaymentConsent.NextTriggeredBy,

    /**
     * Only applicable when next_triggered_by is merchant. One of scheduled, unscheduled.
     * Default: unscheduled
     */
    val merchantTriggerReason: PaymentConsent.MerchantTriggerReason = PaymentConsent.MerchantTriggerReason.UNSCHEDULED,

    /**
     * Only applicable when next_triggered_by is customer. If false, the customer must provide cvc for subsequent payments with this PaymentConsent.
     * Default: false
     */
    val requiresCvc: Boolean? = null
) {

    class Builder(
        private val clientSecret: String,
        private val customerId: String,
        private val paymentMethodType: PaymentMethodType,
        private val nextTriggeredBy: PaymentConsent.NextTriggeredBy
    ) : ObjectBuilder<CreatePaymentConsentParams> {

        private var paymentMethodId: String? = null

        private var merchantTriggerReason: PaymentConsent.MerchantTriggerReason =
            PaymentConsent.MerchantTriggerReason.UNSCHEDULED

        private var requiresCvc: Boolean? = null

        fun setPaymentMethodId(paymentMethodId: String?): Builder = apply {
            this.paymentMethodId = paymentMethodId
        }

        fun setMerchantTriggerReason(merchantTriggerReason: PaymentConsent.MerchantTriggerReason): Builder =
            apply {
                this.merchantTriggerReason = merchantTriggerReason
            }

        fun setRequiresCvc(requiresCvc: Boolean?): Builder = apply {
            this.requiresCvc = requiresCvc
        }

        override fun build(): CreatePaymentConsentParams {
            return CreatePaymentConsentParams(
                clientSecret = clientSecret,
                customerId = customerId,
                paymentMethodId = paymentMethodId,
                paymentMethodType = paymentMethodType,
                nextTriggeredBy = nextTriggeredBy,
                merchantTriggerReason = merchantTriggerReason,
                requiresCvc = requiresCvc
            )
        }
    }

    companion object {

        fun createCardParams(
            clientSecret: String,
            customerId: String,
            paymentMethodId: String,
            nextTriggeredBy: PaymentConsent.NextTriggeredBy,
            merchantTriggerReason: PaymentConsent.MerchantTriggerReason,
            requiresCvc: Boolean
        ): CreatePaymentConsentParams {
            return Builder(
                clientSecret = clientSecret,
                customerId = customerId,
                paymentMethodType = PaymentMethodType.CARD,
                nextTriggeredBy = nextTriggeredBy
            )
                .setMerchantTriggerReason(merchantTriggerReason = merchantTriggerReason)
                .setPaymentMethodId(paymentMethodId)
                .setRequiresCvc(requiresCvc)
                .build()
        }

        fun createThirdPartParams(
            paymentMethodType: PaymentMethodType,
            clientSecret: String,
            customerId: String
        ): CreatePaymentConsentParams {
            return Builder(
                clientSecret = clientSecret,
                customerId = customerId,
                paymentMethodType = paymentMethodType,
                nextTriggeredBy = PaymentConsent.NextTriggeredBy.MERCHANT
            )
                .build()
        }
    }
}
