package com.ohmyguide.app.data.api

import com.google.gson.Gson
import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.model.GuideNavigationResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideSseClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    private var eventSource: EventSource? = null

    fun connect(
        onResponse: (GuideNavigationResponse) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val baseUrl = BuildConfig.BASE_URL.let { if (it.endsWith("/")) it else "$it/" }
        val request = Request.Builder()
            .url("${baseUrl}guide/sse")
            .header("Accept", "text/event-stream")
            .build()

        val sseClient = okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .apply {
                // SSE는 끝나지 않는 스트림 — Level.BODY가 본문 버퍼링 시도하면 이벤트 수신 불가
                interceptors().removeAll { it is HttpLoggingInterceptor }
            }
            .build()


        eventSource = EventSources.createFactory(sseClient)
            .newEventSource(request, object : EventSourceListener() {
                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String,
                ) {
                    if (type == "navigation") {
                        val response = gson.fromJson(data, GuideNavigationResponse::class.java)
                        onResponse(response)
                    }
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?,
                ) {
                    eventSource.cancel()
                    onError(t ?: Exception("SSE connection failed: ${response?.code}"))
                }
            })
    }

    fun close() {
        eventSource?.cancel()
        eventSource = null
    }
}
