package com.airwallex.android

import android.app.Activity
import android.content.Intent
import com.airwallex.android.exception.DccException
import com.airwallex.android.view.SelectCurrencyActivityLaunch

object DccManager {

    var dccCallback: DccCallback? = null

    internal fun handleOnActivityResult(data: Intent?, resultCode: Int) {
        dccCallback?.let {
            try {
                onActivityResult(data, resultCode, it)
            } catch (e: Exception) {
                it.onFailed(DccException(message = e.localizedMessage ?: "Dcc failed."))
            }
        }
    }

    private fun onActivityResult(
        data: Intent?,
        resultCode: Int,
        callback: DccCallback
    ) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val result = SelectCurrencyActivityLaunch.Result.fromIntent(data)
                val paymentIntent = result?.paymentIntent
                if (paymentIntent != null) {
                    callback.onSuccess(paymentIntent)
                } else {
                    callback.onFailed(result?.exception
                        ?: DccException(message = "Dcc failed."))
                }
            }
            Activity.RESULT_CANCELED -> {
                callback.onFailed(DccException(message = "Dcc failed. Reason: User cancel the Dcc"))
            }
        }
    }
}
