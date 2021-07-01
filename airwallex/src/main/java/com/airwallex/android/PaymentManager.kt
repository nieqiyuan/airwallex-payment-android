package com.airwallex.android

import android.content.Context
import android.os.Build
import com.airwallex.android.Airwallex.PaymentListener
import com.airwallex.android.model.*
import com.airwallex.android.view.DccActivityLaunch
import com.airwallex.android.view.ThreeDSecureActivityLaunch
import java.util.*

internal interface PaymentManager {

    var dccCallback: DccCallback?
    var threeDSecureCallback: ThreeDSecureCallback?

    /**
     * Continue the [PaymentIntent] using [ApiRepository.Options], used for 3DS
     *
     * @param options contains the confirm [PaymentIntent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun continuePaymentIntent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentIntent>
    )

    /**
     * Confirm the [PaymentIntent] using [ApiRepository.Options]
     *
     * @param options contains the confirm [PaymentIntent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun confirmPaymentIntent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentIntent>
    )

    /**
     * Retrieve the [PaymentIntent] using [ApiRepository.Options]
     *
     * @param options contains the retrieve [PaymentIntent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun retrievePaymentIntent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentIntent>
    )

    /**
     * Create a Airwallex [PaymentMethod] using [ApiRepository.Options]
     *
     * @param options contains the create [PaymentMethod] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun createPaymentMethod(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentMethod>
    )

    /**
     * Retrieve paRes with id
     */
    fun retrieveParesWithId(
        options: ApiRepository.Options,
        listener: PaymentListener<ThreeDSecurePares>
    )

    /**
     * Confirm [PaymentIntent] with device id
     */
    fun confirmPaymentIntent(
        applicationContext: Context,
        deviceId: String,
        params: ConfirmPaymentIntentParams,
        selectCurrencyActivityLaunch: DccActivityLaunch,
        threeDSecureActivityLaunch: ThreeDSecureActivityLaunch,
        listener: Airwallex.PaymentResultListener<PaymentIntent>
    )

    /**
     * Continue [PaymentIntent] with your selected currency
     */
    fun continueDccPaymentIntent(
        applicationContext: Context,
        threeDSecureActivityLaunch: ThreeDSecureActivityLaunch,
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentIntent>
    )

    /**
     * Create a Airwallex [PaymentConsent] using [ApiRepository.Options]
     *
     * @param options contains the create [PaymentConsent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun createPaymentConsent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentConsent>
    )

    /**
     *  Verify a Airwallex [PaymentConsent]
     */
    fun verifyPaymentConsent(
        applicationContext: Context,
        params: VerifyPaymentConsentParams,
        selectCurrencyActivityLaunch: DccActivityLaunch,
        threeDSecureActivityLaunch: ThreeDSecureActivityLaunch,
        listener: Airwallex.PaymentResultListener<PaymentIntent>
    )

    /**
     * Verify a Airwallex [PaymentConsent] using [ApiRepository.Options]
     *
     * @param options contains the create [PaymentConsent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun verifyPaymentConsent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentConsent>
    )

    /**
     * Disable [PaymentConsent] using [ApiRepository.Options]
     *
     * @param options contains the retrieve [PaymentConsent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun disablePaymentConsent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentConsent>
    )

    /**
     * Retrieve [PaymentConsent] using [ApiRepository.Options]
     *
     * @param options contains the retrieve [PaymentConsent] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun retrievePaymentConsent(
        options: ApiRepository.Options,
        listener: PaymentListener<PaymentConsent>
    )

    /**
     * Retrieve available payment method types
     *
     * @param options contains the retrieve [PaymentMethod] params
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun retrieveAvailablePaymentMethods(
        options: ApiRepository.Options,
        listener: PaymentListener<AvailablePaymentMethodResponse>
    )

    /**
     * Handle next action for 3ds
     *
     * @param applicationContext the Application Context that is to start 3ds screen
     * @param threeDSecureActivityLaunch instance of [ThreeDSecureActivityLaunch]
     * @param paymentIntentId the ID of the [PaymentIntent], required.
     * @param clientSecret the clientSecret of [PaymentIntent], required.
     * @param serverJwt for perform 3ds flow
     * @param device device info
     * @param listener a [PaymentListener] to receive the response or error
     */
    fun handle3DSFlow(
        applicationContext: Context,
        threeDSecureActivityLaunch: ThreeDSecureActivityLaunch,
        paymentIntentId: String,
        clientSecret: String,
        serverJwt: String,
        device: Device?,
        listener: PaymentListener<PaymentIntent>
    )

    companion object {
        private const val PLATFORM = "Android"
        private const val DEVICE_MODEL = "mobile"

        fun buildDeviceInfo(deviceId: String, applicationContext: Context): Device {
            return Device.Builder()
                .setDeviceId(deviceId)
                .setDeviceModel(DEVICE_MODEL)
                .setSdkVersion(AirwallexPlugins.getSdkVersion(applicationContext))
                .setPlatformType(PLATFORM)
                .setDeviceOS(Build.VERSION.RELEASE)
                .build()
        }

        fun buildCardPaymentIntentOptions(
            device: Device,
            params: ConfirmPaymentIntentParams,
            threeDSecure: ThreeDSecure
        ): AirwallexApiRepository.ConfirmPaymentIntentOptions {

            val paymentConsentReference: PaymentConsentReference?
            val paymentMethodReference: PaymentMethodReference?

            if (params.paymentConsentId != null) {
                paymentConsentReference = PaymentConsentReference.Builder()
                    .setId(params.paymentConsentId)
                    .setCvc(params.paymentMethodReference?.cvc)
                    .build()
                paymentMethodReference = null
            } else {
                paymentConsentReference = null
                paymentMethodReference = requireNotNull(params.paymentMethodReference)
            }

            val request = PaymentIntentConfirmRequest.Builder(
                requestId = UUID.randomUUID().toString()
            )
                .setPaymentMethodOptions(
                    PaymentMethodOptions.Builder()
                        .setCardOptions(
                            PaymentMethodOptions.CardOptions.Builder()
                                .setAutoCapture(true)
                                .setThreeDSecure(threeDSecure).build()
                        )
                        .build()
                )
                .setCustomerId(params.customerId)
                .setDevice(device)
                .setPaymentMethodReference(paymentMethodReference)
                .setPaymentConsentReference(paymentConsentReference)
                .build()

            return AirwallexApiRepository.ConfirmPaymentIntentOptions(
                clientSecret = params.clientSecret,
                paymentIntentId = params.paymentIntentId,
                request = request
            )
        }

        fun buildThirdPartPaymentIntentOptions(
            params: ConfirmPaymentIntentParams,
            device: Device
        ): AirwallexApiRepository.ConfirmPaymentIntentOptions {

            val paymentConsentReference: PaymentConsentReference?
            val paymentMethodRequest: PaymentMethodRequest?

            if (params.paymentConsentId != null) {
                paymentConsentReference = PaymentConsentReference.Builder()
                    .setId(params.paymentConsentId)
                    .build()
                paymentMethodRequest = null
            } else {
                paymentConsentReference = null
                paymentMethodRequest = PaymentMethodRequest.Builder(params.paymentMethodType)
                    .setPaymentMethodRequest(params.paymentMethodType, params.name, params.email, params.phone, if (params.bank != null) params.bank.currency else params.currency, params.bank)
                    .build()
            }
            val request = PaymentIntentConfirmRequest.Builder(
                requestId = UUID.randomUUID().toString()
            )
                .setPaymentMethodRequest(paymentMethodRequest)
                .setCustomerId(params.customerId)
                .setDevice(device)
                .setPaymentConsentReference(paymentConsentReference)
                .build()

            return AirwallexApiRepository.ConfirmPaymentIntentOptions(
                clientSecret = params.clientSecret,
                paymentIntentId = params.paymentIntentId,
                request = request
            )
        }
    }
}
