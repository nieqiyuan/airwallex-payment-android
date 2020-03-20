package com.airwallex.paymentacceptance

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object Settings {

    private val context: Context by lazy { ContextProvider.applicationContext }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(ContextProvider.applicationContext)
    }

    val authUrl: String
        get() {
            val defaultAuthUrl =
                ContextProvider.applicationContext.getString(R.string.auth_url_value)
            return sharedPreferences.getString(context.getString(R.string.auth_url), defaultAuthUrl)
                ?: defaultAuthUrl
        }

    val baseUrl: String
        get() {
            val defaultBaseUrl =
                ContextProvider.applicationContext.getString(R.string.base_url_value)
            return sharedPreferences.getString(context.getString(R.string.base_url), defaultBaseUrl)
                ?: defaultBaseUrl
        }

    val apiKey: String
        get() {
            val defaultApiKey = ContextProvider.applicationContext.getString(R.string.api_key_value)
            return sharedPreferences.getString(context.getString(R.string.api_key), defaultApiKey)
                ?: defaultApiKey
        }

    val clientId: String
        get() {
            val defaultClientId =
                ContextProvider.applicationContext.getString(R.string.client_id_value)
            return sharedPreferences.getString(
                context.getString(R.string.client_id),
                defaultClientId
            ) ?: defaultClientId
        }

    val wechatAppId: String
        get() {
            val defaultAppId =
                ContextProvider.applicationContext.getString(R.string.wechat_app_id_value)
            return sharedPreferences.getString(
                context.getString(R.string.wechat_app_id),
                defaultAppId
            )
                ?: defaultAppId
        }

    val wechatAppSignature: String
        get() {
            val defaultAppSignature =
                ContextProvider.applicationContext.getString(R.string.wechat_app_signature_value)
            return sharedPreferences.getString(
                context.getString(R.string.wechat_app_signature),
                defaultAppSignature
            )
                ?: defaultAppSignature
        }
}
