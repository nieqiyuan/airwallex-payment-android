package com.airwallex.android.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.airwallex.android.Airwallex
import com.airwallex.android.DeviceUtils
import com.airwallex.android.R
import com.airwallex.android.exception.AirwallexException
import com.airwallex.android.model.*
import kotlinx.android.synthetic.main.activity_airwallex.*
import kotlinx.android.synthetic.main.activity_payment_checkout.*
import java.util.*

internal class PaymentCheckoutActivity : AirwallexActivity() {

    private val airwallex: Airwallex by lazy {
        Airwallex(
            args.customerSessionConfig.token,
            paymentIntent.clientSecret!!
        )
    }

    private val paymentMethod: PaymentMethod
        get() {
            return args.paymentMethod
        }

    private val paymentIntent: PaymentIntent
        get() {
            return args.customerSessionConfig.paymentIntent
        }

    private val args: PaymentCheckoutActivityStarter.Args by lazy {
        PaymentCheckoutActivityStarter.Args.getExtra(intent)
    }

    override fun homeAsUpIndicatorResId(): Int {
        return R.drawable.airwallex_ic_back
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewStub.layoutResource = R.layout.activity_payment_checkout
        viewStub.inflate()

        tvTotalPrice.text = String.format(
            "%s%.2f",
            Currency.getInstance(paymentIntent.currency).symbol,
            paymentIntent.amount
        )
        paymentMethodItemView.renewalPaymentMethod(paymentMethod, args.cvc)
        paymentMethodItemView.cvcChangedCallback = {
            updateButtonStatus()
        }

        rlPayNow.setOnClickListener {
            onActionSave()
        }
        updateButtonStatus()
    }

    override fun onActionSave() {
        setLoadingProgress(true)
        val paymentIntentParams: PaymentIntentParams = when (paymentMethod.type) {
            PaymentMethodType.CARD -> {
                PaymentIntentParams.Builder()
                    .setRequestId(UUID.randomUUID().toString())
                    .setCustomerId(paymentIntent.customerId)
                    .setDevice(DeviceUtils.device)
                    .setPaymentMethodReference(
                        PaymentMethodReference.Builder()
                            .setId(paymentMethod.id)
                            .setCvc(paymentMethodItemView.cvc)
                            .build()
                    )
                    .setPaymentMethodOptions(
                        PaymentMethodOptions.Builder()
                            .setCardOptions(
                                PaymentMethodOptions.CardOptions.Builder()
                                    .setAutoCapture(true)
                                    .setThreeDs(
                                        PaymentMethodOptions.CardOptions.ThreeDs.Builder()
                                            .setOption(false)
                                            .build()
                                    ).build()
                            )
                            .build()
                    )
                    .build()
            }
            PaymentMethodType.WECHAT -> {
                PaymentIntentParams.Builder()
                    .setRequestId(UUID.randomUUID().toString())
                    .setCustomerId(paymentIntent.customerId)
                    .setDevice(DeviceUtils.device)
                    .setPaymentMethod(paymentMethod)
                    .build()
            }
        }

        // Start Confirm PaymentIntent
        airwallex.confirmPaymentIntent(
            paymentIntentId = paymentIntent.id,
            paymentIntentParams = paymentIntentParams,
            callback = object : Airwallex.PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    finishPaymentCheckout(paymentIntent = paymentIntent, type = paymentMethod.type)
                }

                override fun onFailed(exception: AirwallexException) {
                    finishPaymentCheckout(exception = exception)
                }
            }
        )
    }

    private fun finishPaymentCheckout(
        paymentIntent: PaymentIntent? = null,
        type: PaymentMethodType? = null,
        exception: AirwallexException? = null
    ) {
        setLoadingProgress(false)
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtras(
                    PaymentCheckoutActivityStarter.Result(
                        paymentIntent = paymentIntent,
                        paymentMethodType = type,
                        exception = exception
                    ).toBundle()
                )
        )
        finish()
    }

    private fun updateButtonStatus() {
        rlPayNow.isEnabled = paymentMethodItemView.isValid
    }
}