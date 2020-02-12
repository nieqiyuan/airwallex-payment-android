package com.airwallex.paymentacceptance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.airwallex.android.Airwallex
import com.airwallex.android.exception.AirwallexException
import com.airwallex.android.model.*
import kotlinx.android.synthetic.main.activity_payment_checkout.*
import okhttp3.*
import java.io.IOException
import java.util.*

class PaymentCheckoutActivity : PaymentBaseActivity() {

    private val paymentIntent: PaymentIntent by lazy {
        intent.getParcelableExtra(PAYMENT_INTENT) as PaymentIntent
    }

    override val inPaymentFlow: Boolean
        get() = true

    private var paymentMethod: PaymentMethod? = null

    companion object {

        private const val TAG = "PaymentPayActivity"

        fun startActivity(
            activity: Activity,
            paymentIntent: PaymentIntent
        ) {
            activity.startActivity(
                Intent(activity, PaymentCheckoutActivity::class.java)
                    .putExtra(PAYMENT_INTENT, paymentIntent)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_checkout)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        tvTotalPrice.text = String.format("$%.2f", paymentIntent.amount)

        rlPlay.setOnClickListener {
            paymentMethod?.let {
                startConfirmPaymentIntent(it)
            }
        }

        paymentMethodItemView.paymentIntent = paymentIntent
        paymentMethodItemView.renewalPaymentMethod((paymentMethod))
        shippingItemView.renewalShipping(SampleApplication.instance.shipping)

        rlPlay.isEnabled = paymentMethod != null
        btnPlay.isEnabled = rlPlay.isEnabled
    }

    private fun startConfirmPaymentIntent(paymentMethod: PaymentMethod) {
        when (paymentMethod.type) {
            PaymentMethodType.CARD -> {
                // Need fill CVC
                PaymentCheckoutCvcActivity.startActivityForResult(
                    this,
                    paymentMethod,
                    paymentIntent,
                    REQUEST_CONFIRM_CVC_CODE
                )
            }
            PaymentMethodType.WECHAT -> {
                loading.visibility = View.VISIBLE
                val paymentIntentParams: PaymentIntentParams

                val paymentMethodOptions: PaymentMethodOptions = PaymentMethodOptions.Builder()
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

                paymentIntentParams = PaymentIntentParams.Builder()
                    .setRequestId(UUID.randomUUID().toString())
                    .setDevice(PaymentData.device)
                    .setPaymentMethod(paymentMethod)
                    .setPaymentMethodOptions(paymentMethodOptions)
                    .build()

                // Start Confirm PaymentIntent
                val airwallex = Airwallex(Store.token, paymentIntent.clientSecret!!)
                airwallex.confirmPaymentIntent(
                    paymentIntentId = paymentIntent.id!!,
                    paymentIntentParams = paymentIntentParams,
                    callback = object : Airwallex.PaymentIntentCallback {
                        override fun onSuccess(paymentIntent: PaymentIntent) {
                            handlePaymentWithWechat(airwallex, paymentIntent)
                        }

                        override fun onFailed(exception: AirwallexException) {
                            loading.visibility = View.GONE
                            Toast.makeText(
                                this@PaymentCheckoutActivity,
                                exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                )
            }
        }
    }

    private fun handlePaymentWithWechat(airwallex: Airwallex, paymentIntent: PaymentIntent) {
        val nextAction = paymentIntent.nextAction
        if (nextAction?.data == null
        ) {
            Toast.makeText(
                this@PaymentCheckoutActivity,
                "Server error, NextAction is null...",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val prepayId = nextAction.data?.prepayId

        Log.d(TAG, "prepayId $prepayId")

        if (prepayId?.startsWith("http") == true) {
            Log.d(TAG, "Confirm PaymentIntent success, launch MOCK Wechat pay.")
            // launch mock wechat pay
            val client = OkHttpClient()
            val builder = Request.Builder()
            builder.url(prepayId)
            client.newCall(builder.build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "User cancel the Wechat payment")
                    loading.visibility = View.GONE
                    Toast.makeText(
                        this@PaymentCheckoutActivity,
                        "Failed to mock wechat pay, reason: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    retrievePaymentIntent(airwallex)
                }
            })
        } else {
            Log.d(TAG, "Confirm PaymentIntent success, launch REAL Wechat pay.")
            // launch wechat pay
            WXPay.instance.launchWeChat(
                context = this@PaymentCheckoutActivity,
                appId = Constants.APP_ID,
                data = paymentIntent.nextAction!!.data!!,
                listener = object : PayListener {
                    override fun onSuccess() {
                        retrievePaymentIntent(airwallex)
                    }

                    override fun onFailure(errCode: String?, errMessage: String?) {
                        Log.e(TAG, "Wechat pay failed, error $errMessage")
                        loading.visibility = View.GONE
                        Toast.makeText(
                            this@PaymentCheckoutActivity,
                            "errCode $errCode, errMessage $errMessage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onCancel() {
                        Log.e(TAG, "User cancel the Wechat payment")
                        loading.visibility = View.GONE
                        Toast.makeText(
                            this@PaymentCheckoutActivity,
                            "User cancel the payment",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun retrievePaymentIntent(airwallex: Airwallex) {
        Log.d(
            TAG,
            "Start retrieve PaymentIntent ${paymentIntent.id}"
        )
        airwallex.retrievePaymentIntent(
            paymentIntentId = paymentIntent.id!!,
            callback = object : Airwallex.PaymentIntentCallback {
                override fun onSuccess(paymentIntent: PaymentIntent) {
                    Log.d(
                        TAG,
                        "Retrieve PaymentIntent success, PaymentIntent status: ${paymentIntent.status}"
                    )

                    loading.visibility = View.GONE
                    if (paymentIntent.status == "SUCCEEDED") {
                        showPaymentSuccess()
                    } else {
                        showPaymentError()
                    }
                }

                override fun onFailed(exception: AirwallexException) {
                    Log.e(TAG, "Retrieve PaymentIntent failed")
                    loading.visibility = View.GONE

                    // TODO Need Retry?
                    showPaymentError()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        paymentMethodItemView.onActivityResult(requestCode, resultCode, data) {
            this.paymentMethod = it
            rlPlay.isEnabled = paymentMethod != null
            btnPlay.isEnabled = rlPlay.isEnabled
        }

        shippingItemView.onActivityResult(requestCode, resultCode, data) {
            SampleApplication.instance.shipping = it
        }
    }
}