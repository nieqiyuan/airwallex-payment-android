package com.airwallex.android

import android.app.Activity
import android.content.Intent
import com.airwallex.android.model.*
import com.airwallex.android.view.AddPaymentMethodActivityStarter
import com.airwallex.android.view.PaymentCheckoutActivityStarter
import com.airwallex.android.view.PaymentMethodsActivityStarter
import com.airwallex.android.view.PaymentShippingActivityStarter

class PaymentSession constructor(
    private val context: Activity,
    private val configuration: PaymentSessionConfiguration
) {

    interface PaymentResult {
        fun onCancelled()
    }

    interface PaymentShippingResult : PaymentResult {
        fun onSuccess(shipping: Shipping)
    }

    interface PaymentIntentResult : PaymentResult {
        fun onSuccess(paymentIntent: PaymentIntent, paymentMethodType: PaymentMethodType)
        fun onFailed(error: AirwallexError)
    }

    interface PaymentMethodResult : PaymentResult {
        fun onSuccess(paymentMethod: PaymentMethod, cvc: String?)
    }

    @Throws(NullPointerException::class)
    fun presentPaymentFlow() {
        val paymentIntent = requireNotNull(configuration.paymentIntent)
        val token = requireNotNull(configuration.token)
        PaymentMethodsActivityStarter(context)
            .startForResult(
                PaymentMethodsActivityStarter.Args.Builder()
                    .setPaymentIntent(paymentIntent)
                    .setToken(token)
                    .setIncludeCheckoutFlow(true)
                    .build()
            )
    }

    fun presentShippingFlow() {
        PaymentShippingActivityStarter(context)
            .startForResult(
                PaymentShippingActivityStarter.Args.Builder()
                    .setShipping(configuration.shipping)
                    .build()
            )
    }

    @Throws(NullPointerException::class)
    fun presentAddPaymentMethodFlow() {
        val paymentIntent = requireNotNull(configuration.paymentIntent)
        val token = requireNotNull(configuration.token)
        AddPaymentMethodActivityStarter(context)
            .startForResult(
                AddPaymentMethodActivityStarter.Args.Builder()
                    .setPaymentIntent(paymentIntent)
                    .setToken(token)
                    .build()
            )
    }

    @Throws(NullPointerException::class)
    fun presentSelectPaymentMethodFlow() {
        val paymentIntent = requireNotNull(configuration.paymentIntent)
        val token = requireNotNull(configuration.token)
        PaymentMethodsActivityStarter(context)
            .startForResult(
                PaymentMethodsActivityStarter.Args.Builder()
                    .setPaymentIntent(paymentIntent)
                    .setToken(token)
                    .setIncludeCheckoutFlow(false)
                    .build()
            )
    }

    @Throws(NullPointerException::class)
    fun presentPaymentCheckoutFlow() {
        val paymentIntent = requireNotNull(configuration.paymentIntent)
        val token = requireNotNull(configuration.token)
        val paymentMethod = requireNotNull(configuration.paymentMethod)
        PaymentCheckoutActivityStarter(context)
            .startForResult(
                PaymentCheckoutActivityStarter.Args.Builder()
                    .setPaymentIntent(paymentIntent)
                    .setToken(token)
                    .setPaymentMethod(paymentMethod)
                    .build()
            )
    }

    fun handlePaymentCheckoutResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: PaymentIntentResult
    ) {
        handleResult(
            requestCode,
            resultCode,
            data,
            callback
        )
    }

    fun handleAddPaymentMethodResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: PaymentMethodResult
    ) {
        handleResult(
            requestCode,
            resultCode,
            data,
            callback
        )
    }

    fun handleSelectPaymentMethodResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: PaymentMethodResult
    ) {
        handleResult(
            requestCode,
            resultCode,
            data,
            callback
        )
    }

    fun handlePaymentShippingResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: PaymentShippingResult
    ) {
        handleResult(
            requestCode,
            resultCode,
            data,
            callback
        )
    }

    fun handlePaymentIntentResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: PaymentIntentResult
    ) {
        handleResult(
            requestCode,
            resultCode,
            data,
            callback
        )
    }

    private fun handleResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        callback: PaymentResult? = null
    ): Boolean {
        if (!VALID_REQUEST_CODES.contains(requestCode)) {
            return false
        }

        when (resultCode) {
            Activity.RESULT_OK -> {
                return when (requestCode) {
                    AddPaymentMethodActivityStarter.REQUEST_CODE -> {
                        val result = AddPaymentMethodActivityStarter.Result.fromIntent(data)
                        (callback as? PaymentMethodResult)?.onSuccess(
                            requireNotNull(result?.paymentMethod),
                            result?.cvc
                        )
                        true
                    }
                    PaymentShippingActivityStarter.REQUEST_CODE -> {
                        val result = PaymentShippingActivityStarter.Result.fromIntent(data)
                        (callback as? PaymentShippingResult)?.onSuccess(requireNotNull(result?.shipping))
                        true
                    }
                    PaymentMethodsActivityStarter.REQUEST_CODE -> {
                        val result = PaymentMethodsActivityStarter.Result.fromIntent(data)
                        if (result?.error != null) {
                            (callback as? PaymentIntentResult)?.onFailed(result.error)
                        } else {
                            if (result?.paymentMethod != null) {
                                (callback as? PaymentMethodResult)?.onSuccess(
                                    requireNotNull(result.paymentMethod),
                                    result.cvc
                                )
                            } else {
                                (callback as? PaymentIntentResult)?.onSuccess(
                                    requireNotNull(result?.paymentIntent),
                                    requireNotNull(result?.paymentMethodType)
                                )
                            }
                        }
                        true
                    }
                    PaymentCheckoutActivityStarter.REQUEST_CODE -> {
                        val result = PaymentCheckoutActivityStarter.Result.fromIntent(data)
                        if (result?.error != null) {
                            (callback as? PaymentIntentResult)?.onFailed(result.error)
                        } else {
                            (callback as? PaymentIntentResult)?.onSuccess(
                                requireNotNull(result?.paymentIntent),
                                requireNotNull(result?.paymentMethodType)
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
            Activity.RESULT_CANCELED -> {
                return when (requestCode) {
                    AddPaymentMethodActivityStarter.REQUEST_CODE -> {
                        (callback as? PaymentMethodResult)?.onCancelled()
                        true
                    }
                    PaymentShippingActivityStarter.REQUEST_CODE -> {
                        (callback as? PaymentShippingResult)?.onCancelled()
                        true
                    }
                    PaymentMethodsActivityStarter.REQUEST_CODE -> {
                        (callback as? PaymentIntentResult)?.onCancelled()
                        true
                    }
                    PaymentCheckoutActivityStarter.REQUEST_CODE -> {
                        (callback as? PaymentIntentResult)?.onCancelled()
                        true
                    }
                    else -> false
                }
            }
            else -> return false
        }
    }

    internal companion object {

        private val VALID_REQUEST_CODES = setOf(
            PaymentMethodsActivityStarter.REQUEST_CODE,
            PaymentShippingActivityStarter.REQUEST_CODE,
            AddPaymentMethodActivityStarter.REQUEST_CODE,
            PaymentCheckoutActivityStarter.REQUEST_CODE
        )
    }
}