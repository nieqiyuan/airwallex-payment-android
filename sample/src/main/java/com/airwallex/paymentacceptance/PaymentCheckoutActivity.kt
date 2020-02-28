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

    private val device = Device.Builder()
        .setBrowserInfo("Chrome/76.0.3809.100")
        .setCookiesAccepted("true")
        .setDeviceId("IMEI-4432fsdafd31243244fdsafdfd653")
        .setHostName("www.airwallex.com")
        .setHttpBrowserEmail("jim631@sina.com")
        .setHttpBrowserType("chrome")
        .setIpAddress("123.90.0.1")
        .setIpNetworkAddress("128.0.0.0")
        .build()

    private val paymentIntent: PaymentIntent by lazy {
        intent.getParcelableExtra(PAYMENT_INTENT) as PaymentIntent
    }

    private val airwallex: Airwallex by lazy {
        Airwallex("", paymentIntent.clientSecret)
    }

    override val inPaymentFlow: Boolean
        get() = true

    private var paymentMethod: PaymentMethod? = null

    companion object {
        private const val TAG = "PaymentPayActivity"
        private const val PAYMENT_INTENT = "payment_intent"

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
                // TODO Should update payment method with billing info
//                val shipping = paymentIntent.order.shipping
//                val billing = this.billing ?: shipping?.let {
//                    PaymentMethod.Billing.Builder()
//                        .setFirstName(it.firstName)
//                        .setLastName(it.lastName)
//                        .setPhone(it.phone)
//                        .setEmail(it.email)
//                        .setAddress(
//                            it.address?.apply {
//                                Address.Builder()
//                                    .setCountryCode(countryCode)
//                                    .setState(state)
//                                    .setCity(city)
//                                    .setStreet(street)
//                                    .setPostcode(postcode)
//                                    .build()
//                            }
//                        )
//                        .build()
//                }

                startConfirmPaymentIntent(it)
            }
        }

        paymentMethodItemView.paymentIntent = paymentIntent
        paymentMethodItemView.renewalPaymentMethod(paymentMethod)
        paymentMethodItemView.cvcChangedCallback = {
            updateButtonStatus()
        }

        // update payment method item
        val paymentMethods = paymentIntent.customerPaymentMethods
        if (paymentMethods != null && paymentMethods.size == 1) {
            paymentMethod = paymentMethods[0]
            paymentMethodItemView.renewalPaymentMethod(paymentMethod)
        }

        // update button status
        updateButtonStatus()
    }

    private fun startConfirmPaymentIntent(paymentMethod: PaymentMethod) {
        loading.visibility = View.VISIBLE
        val paymentIntentParams: PaymentIntentParams = when (paymentMethod.type) {
            PaymentMethodType.CARD -> {
                PaymentIntentParams.Builder()
                    .setRequestId(UUID.randomUUID().toString())
                    .setCustomerId(paymentIntent.customerId)
                    .setDevice(device)
                    .setPaymentMethodReference(
                        PaymentMethodReference.Builder()
                            .setId(paymentMethod.id)
                            .setCvc("123")
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
                    .setDevice(device)
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
                    handlePaymentResult(paymentMethod, paymentIntent) {
                        retrievePaymentIntent(airwallex)
                    }
                }

                override fun onFailed(exception: AirwallexException) {
                    loading.visibility = View.GONE
                    showPaymentError()
                }
            }
        )
    }

    private fun handlePaymentResult(
        paymentMethod: PaymentMethod,
        paymentIntent: PaymentIntent,
        completion: () -> Unit
    ) {
        when (paymentMethod.type) {
            PaymentMethodType.CARD -> {
                completion.invoke()
            }
            PaymentMethodType.WECHAT -> {
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
                            completion.invoke()
                        }
                    })
                } else {
                    Log.d(TAG, "Confirm PaymentIntent success, launch REAL Wechat pay.")
                    val data = paymentIntent.nextAction?.data
                    if (data == null) {
                        Toast.makeText(
                            this@PaymentCheckoutActivity,
                            "No Wechat data!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    // launch wechat pay
                    WXPay.instance.launchWeChat(
                        context = this@PaymentCheckoutActivity,
                        appId = Constants.APP_ID,
                        data = data,
                        listener = object : WXPay.WechatPaymentListener {
                            override fun onSuccess() {
                                completion.invoke()
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
        }
    }

    private fun retrievePaymentIntent(airwallex: Airwallex) {
        Log.d(
            TAG,
            "Start retrieve PaymentIntent ${paymentIntent.id}"
        )
        airwallex.retrievePaymentIntent(
            paymentIntentId = paymentIntent.id,
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
                    showPaymentError()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        paymentMethodItemView.onActivityResult(requestCode, resultCode, data) {
            this.paymentMethod = it
            updateButtonStatus()
        }
    }

    private fun updateButtonStatus() {
        rlPlay.isEnabled = paymentMethodItemView.isValid
    }
}