package com.airwallex.android.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.airwallex.android.Airwallex
import com.airwallex.android.CurrencyUtils.formatPrice
import com.airwallex.android.R
import com.airwallex.android.exception.AirwallexException
import com.airwallex.android.model.AirwallexError
import com.airwallex.android.model.PaymentIntent
import com.airwallex.android.model.PaymentMethod
import kotlinx.android.synthetic.main.activity_payment_checkout.*

/**
 * Activity to confirm payment intent
 */
class PaymentCheckoutActivity : AirwallexCheckoutBaseActivity() {

    override val airwallex: Airwallex by lazy {
        Airwallex()
    }

    private val args: PaymentCheckoutActivityLaunch.Args by lazy {
        PaymentCheckoutActivityLaunch.Args.getExtra(intent)
    }

    private val paymentMethod: PaymentMethod by lazy {
        args.paymentMethod
    }

    override val paymentIntent: PaymentIntent by lazy {
        args.paymentIntent
    }

    override val cvc: String?
        get() = paymentMethodItemView.cvc

    override fun homeAsUpIndicatorResId(): Int {
        return R.drawable.airwallex_ic_back
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvTotalPrice.text = formatPrice(paymentIntent.currency, paymentIntent.amount)
        paymentMethodItemView.renewalPaymentMethod(paymentMethod, args.cvc)
        paymentMethodItemView.cvcChangedCallback = {
            updateButtonStatus()
        }

        rlPayNow.setOnClickListener {
            startConfirmPaymentIntent()
        }
        updateButtonStatus()
    }

    override val layoutResource: Int
        get() = R.layout.activity_payment_checkout

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        airwallex.onPaymentIntentResult(requestCode, resultCode, data)
    }

    private fun startConfirmPaymentIntent() {
        confirmPaymentIntent(paymentMethod = paymentMethod,
            listener = object : Airwallex.PaymentListener<PaymentIntent> {
                override fun onSuccess(response: PaymentIntent) {
                    finishWithPaymentIntent(paymentIntent = response)
                }

                override fun onFailed(exception: AirwallexException) {
                    finishWithPaymentIntent(error = exception.error)
                }
            })
    }

    private fun finishWithPaymentIntent(
        paymentIntent: PaymentIntent? = null,
        error: AirwallexError? = null
    ) {
        setLoadingProgress(false)
        setResult(
            Activity.RESULT_OK, Intent().putExtras(
            PaymentCheckoutActivityLaunch.Result(
                paymentIntent = paymentIntent,
                error = error
            ).toBundle()
        )
        )
        finish()
    }

    private fun updateButtonStatus() {
        rlPayNow.isEnabled = paymentMethodItemView.isValid
    }
}
