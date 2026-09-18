package com.morningbrief.app.repository.tdx

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TdxAuthApi {
    @FormUrlEncoded
    @POST("auth/realms/TDXConnect/protocol/openid-connect/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): TdxTokenResponse
}

interface TdxMetroApi {
    @GET("api/basic/v2/Rail/Metro/StationTimeTable/TRTC")
    suspend fun getStationTimetable(
        @Header("Authorization") authorization: String,
        @Query("\$filter") filter: String,
        @Query("\$format") format: String = "JSON"
    ): List<TdxStationTimetable>
}
