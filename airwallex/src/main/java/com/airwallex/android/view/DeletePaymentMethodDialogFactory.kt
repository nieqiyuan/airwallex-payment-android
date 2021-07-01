package com.airwallex.android.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.airwallex.android.R
import com.airwallex.android.model.PaymentConsent
import java.util.*

internal class DeletePaymentMethodDialogFactory internal constructor(
    private val context: Context,
    private val adapter: PaymentMethodsAdapter,
    private val onDeletedCallback: (PaymentConsent) -> Unit
) {
    @SuppressLint("StringFormatInvalid")
    @JvmSynthetic
    fun create(paymentConsent: PaymentConsent): AlertDialog {
        val title = paymentConsent.paymentMethod?.card?.let {
            context.resources.getString(
                R.string.delete_payment_method_prompt_title,
                String.format("%s •••• %s", it.brand?.uppercase(Locale.ROOT), it.last4)
            )
        }
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setPositiveButton(R.string.delete_payment_method_positive) { _, _ ->
                onDeletedPaymentMethod(paymentConsent)
            }
            .setNegativeButton(R.string.delete_payment_method_negative) { _, _ ->
                adapter.resetPaymentMethod(paymentConsent)
            }
            .setOnCancelListener {
                adapter.resetPaymentMethod(paymentConsent)
            }
            .create()
    }

    @JvmSynthetic
    internal fun onDeletedPaymentMethod(paymentConsent: PaymentConsent) {
        onDeletedCallback(paymentConsent)
    }
}
