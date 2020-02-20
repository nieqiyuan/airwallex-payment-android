package com.airwallex.paymentacceptance

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.airwallex.android.model.Order
import com.airwallex.android.model.PaymentIntent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_payment_cart.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class PaymentCartActivity : PaymentBaseActivity() {

    private val compositeSubscription = CompositeDisposable()

    private val api: Api by lazy {
        ApiFactory(Constants.BASE_URL).create()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, PaymentCartActivity::class.java))
            (context as Activity).finish()
        }
    }

    override val inPaymentFlow: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_cart)

        btnCheckout.setOnClickListener {
            authAndCreatePaymentIntent()
        }
    }

    override fun onDestroy() {
        compositeSubscription.dispose()
        super.onDestroy()
    }

    private fun authAndCreatePaymentIntent() {
        compositeSubscription.add(
            api.authentication(
                apiKey = Constants.API_KEY,
                clientId = Constants.CLIENT_ID
            )
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    loading.visibility = View.VISIBLE
                }
//                .observeOn(Schedulers.io())
//                .flatMap {
//                    val responseData = JSONObject(it.string())
//                    val token = responseData["token"].toString()
//                    Store.token = token
//                    api.createCustomer(
//                        authorization = "Bearer $token",
//                        params = mutableMapOf(
//                            "request_id" to UUID.randomUUID().toString(),
//                            "merchant_customer_id" to "79fc248c-a2fc-4863-b1ea-fac2e2c16dac",
//                            "first_name" to "John",
//                            "last_name" to "Doe",
//                            "email" to "john.doe@airwallex.com",
//                            "phone_number" to "13800000000",
//                            "additional_info" to mapOf(
//                                "registered_via_social_media" to false,
//                                "registration_date" to "2019-09-18",
//                                "first_successful_order_date" to "2019-09-18"
//                            ),
//                            "metadata" to mapOf(
//                                "id" to 1
//                            )
//                        )
//                    )
//                }
                .observeOn(Schedulers.io())
                .flatMap {
                    val responseData = JSONObject(it.string())
                    val token = responseData["token"].toString()
                    Store.token = token
                    val customerId = "cus_Dn6mVcMeTEkJgYuu9o5xEcxWRah"
                    val products = SampleApplication.instance.products
                    val shipping = SampleApplication.instance.shipping
                    api.createPaymentIntent(
                        authorization = "Bearer ${Store.token}",
                        params = mutableMapOf(
                            "request_id" to UUID.randomUUID().toString(),
                            "amount" to products.sumByDouble { product ->
                                product.unitPrice ?: 0 * (product.quantity ?: 0).toDouble()
                            },
                            "currency" to "USD",
                            "merchant_order_id" to UUID.randomUUID().toString(),
                            "order" to Order.Builder()
                                .setProducts(products)
                                .setShipping(shipping)
                                .setType("physical_goods")
                                .build(),
                            "customer_id" to customerId,
                            "descriptor" to "Airwallex - T-shirt",
                            "metadata" to mapOf("id" to 1)
                        )
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { handleResponse(it) },
                    { handleError(it) }
                )
        )
    }

    private fun handleError(err: Throwable) {
        loading.visibility = View.GONE
        Toast.makeText(this, err.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    private fun handleResponse(paymentIntent: PaymentIntent) {
        loading.visibility = View.GONE
        try {
            PaymentCheckoutActivity.startActivity(
                this,
                paymentIntent
            )
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fgOrderSummary.onActivityResult(requestCode, resultCode, data)
    }
}