package com.morningbrief.app.repository.tdx

import com.morningbrief.app.BuildConfig

object TdxConfig {
    /**
     * 您也可以直接在此處填寫 Client ID 與 Client Secret（選填，若 local.properties 已設定則自動生效）：
     */
    const val DIRECT_CLIENT_ID: String = ""
    const val DIRECT_CLIENT_SECRET: String = ""

    fun getClientId(): String {
        return DIRECT_CLIENT_ID.ifBlank { BuildConfig.TDX_CLIENT_ID }.trim()
    }

    fun getClientSecret(): String {
        return DIRECT_CLIENT_SECRET.ifBlank { BuildConfig.TDX_CLIENT_SECRET }.trim()
    }

    fun hasCredentials(): Boolean {
        return getClientId().isNotBlank() && getClientSecret().isNotBlank()
    }
}
