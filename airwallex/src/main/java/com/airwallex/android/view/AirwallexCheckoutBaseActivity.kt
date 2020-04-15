package com.airwallex.android.view

import android.content.Intent
import com.airwallex.android.Airwallex
import com.airwallex.android.ConfirmPaymentIntentParams
import com.airwallex.android.model.PaymentIntent
import com.airwallex.android.model.PaymentMethod
import com.airwallex.android.model.PaymentMethodReference
import com.airwallex.android.model.PaymentMethodType

internal abstract class AirwallexCheckoutBaseActivity : AirwallexActivity() {

    abstract val airwallex: Airwallex

    abstract val paymentIntent: PaymentIntent

    abstract val cvc: String?

    override fun onActionSave() {
        // Ignore
    }

    protected fun confirmPaymentIntent(
        paymentMethod: PaymentMethod,
        callback: Airwallex.PaymentListener<PaymentIntent>
    ) {
        setLoadingProgress(true)

        val params = when (paymentMethod.type) {
            PaymentMethodType.WECHAT -> {
                ConfirmPaymentIntentParams.Builder(
                    // the ID of the `PaymentIntent`, required.
                    paymentIntentId = paymentIntent.id,
                    // the clientSecret of `PaymentIntent`, required.
                    clientSecret = requireNotNull(paymentIntent.clientSecret)
                )
                    // the customerId of `PaymentIntent`, optional.
                    .setCustomerId(paymentIntent.customerId)
                    .setPaymentMethod(PaymentMethodType.WECHAT)
                    .build()
            }
            PaymentMethodType.CARD -> {
                ConfirmPaymentIntentParams.Builder(
                    // the ID of the `PaymentIntent`, required.
                    paymentIntentId = paymentIntent.id,
                    // the clientSecret of `PaymentIntent`, required.
                    clientSecret = requireNotNull(paymentIntent.clientSecret)
                )
                    // the customerId of `PaymentIntent`, optional.
                    .setCustomerId(paymentIntent.customerId)
                    .setPaymentMethod(
                        PaymentMethodType.CARD,
                        PaymentMethodReference(
                            paymentMethod.id,
                            requireNotNull(cvc)
                        )
                    )
                    .build()
            }
        }
        airwallex.confirmPaymentIntent(this, params, callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {
            airwallex.handleConfirmPaymentIntentResult(requestCode, resultCode, data)
        }
    }
}