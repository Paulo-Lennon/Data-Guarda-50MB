package com.paulo.dataguard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {

    enum class NetworkType {
        WIFI,
        MOBILE,
        NONE
    }

    fun getNetworkType(context: Context): NetworkType {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
            ?: return NetworkType.NONE

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
                ?: return NetworkType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                NetworkType.WIFI

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                NetworkType.MOBILE

            else ->
                NetworkType.NONE
        }
    }

    fun isWifiConnected(context: Context): Boolean =
        getNetworkType(context) == NetworkType.WIFI

    fun isMobileConnected(context: Context): Boolean =
        getNetworkType(context) == NetworkType.MOBILE
}