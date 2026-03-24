package com.ohmyguide.app.di

import android.content.Context
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.api.ApiService
import com.ohmyguide.app.data.api.AuthInterceptor
import com.ohmyguide.app.data.api.BusanBimsApi
import com.ohmyguide.app.data.api.NaverDrivingApi
import com.ohmyguide.app.data.api.NaverWalkingApi
import com.ohmyguide.app.data.api.OdsayApi
import com.ohmyguide.app.data.api.OpenMeteoApi
import com.ohmyguide.app.data.api.TmapApi
import com.ohmyguide.app.data.local.TokenDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenDataStore(@ApplicationContext context: Context): TokenDataStore {
        return TokenDataStore(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.BASE_URL.let { if (it.endsWith("/")) it else "$it/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOdsayApi(client: OkHttpClient): OdsayApi {
        return Retrofit.Builder()
            .baseUrl("https://api.odsay.com/v1/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OdsayApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNaverDrivingApi(client: OkHttpClient): NaverDrivingApi {
        return Retrofit.Builder()
            .baseUrl("https://maps.apigw.ntruss.com/map-direction/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverDrivingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNaverWalkingApi(client: OkHttpClient): NaverWalkingApi {
        return Retrofit.Builder()
            .baseUrl("https://maps.apigw.ntruss.com/map-direction-15/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverWalkingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTmapApi(client: OkHttpClient): TmapApi {
        return Retrofit.Builder()
            .baseUrl("https://apis.openapi.sk.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBusanBimsApi(client: OkHttpClient): BusanBimsApi {
        return Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/6260000/BusanBIMS/")
            .client(client)
            .build()
            .create(BusanBimsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenMeteoApi(client: OkHttpClient): OpenMeteoApi {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}
