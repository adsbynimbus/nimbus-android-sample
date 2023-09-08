package com.adsbynimbus.android.sample

import android.content.Context
import android.os.Bundle
import android.os.Trace
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.Nimbus
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BenchmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
           // Nimbus.initialize(this@BenchmarkActivity, "dev", "f7370249-a77a-406f-9359-ec71be576260")
            RequestManager.interceptors.clear()
            Nimbus.sessionId = "1"
            val nimbusAdManager = object : RequestManager { }
            syncRequest(this@BenchmarkActivity, nimbusAdManager)
            delay(200)
            asyncRequests(this@BenchmarkActivity, nimbusAdManager)
            finish()
        }
    }
}

suspend fun CoroutineScope.asyncRequests(context: Context, nimbusAdManager: RequestManager) {
    (1..3).map { id ->
        val request = NimbusRequest.forBannerAd("test$id", Format.BANNER_320_50)
      //  Trace.beginAsyncSection("Async", id)
        async {
            val response = runCatching {
                withTimeoutOrNull(1500) { nimbusAdManager.makeRequest(context, request) }
            }.getOrNull()
    //        Trace.endAsyncSection("Async", id)
            response
        }
    }.awaitAll()
}

suspend fun CoroutineScope.syncRequest(context: Context, nimbusAdManager: RequestManager) {
    (1..3).map { id ->
        val request = NimbusRequest.forBannerAd("test$id", Format.BANNER_320_50)
   //     Trace.beginSection("Sync")
        runCatching {
            withTimeoutOrNull(1500) { nimbusAdManager.makeRequest(context, request) }
        }.getOrNull()
  //      Trace.endSection()
    }
}
