package com.rs.myvocabulary.composeable

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlin.text.trimIndent

val script = mapOf(
    "linkedin" to """
        (function() {
            const postContent = document.querySelector("p.attributed-text-segment-list__content")
            const feedImagesContent = document.querySelector(".feed-images-content")
            const ignoreImages = ["data:image/svg", "images/emoji.php"]
            const feedImages = feedImagesContent?.querySelectorAll("li > img") ?? []
            let images = [] 
            
            for (let feedImage of feedImages) {
                const url = feedImage.getAttribute("src") || feedImage.getAttribute("data-delayed-url")
                if (url && !ignoreImages.some(ignorePattern => url.includes(ignorePattern))) {
                    images.push(url);
                }
            }
    
            const postText = postContent?.innerText
             AndroidBridge?.sendData(JSON.stringify({
                content: postText,
                imageUrls: images,
                cover: images[0] || ""
            }))
       }())
    """.trimIndent(),

    "facebook" to """
         (function () {
           return new Promise( async (resolve)=>{
                 try{
                     const elements = document.querySelectorAll('[role="button"]')
                       let btn;
                       elements?.forEach(item=>{
                           const a = item.innerText === "See more" 
                           console.log("a", item.innerText)
                           if(a){
                               btn = item
                                btn?.click()
                                 console.log("btn", btn)
                               return;
                           }  
                       })      
                       
                       await new Promise(aa => setTimeout(aa, 300));

                      const ignoreImages = ["data:image/svg", "images/emoji.php"];
                       const article = document.querySelector('[role="article"]');
                       const postContent  = article?.textContent?.trim() ?? "";
        
                       const images = [...(article?.querySelectorAll('img') || [])]
                         .map(img => img.getAttribute('src'))
                         .filter(src => src && !ignoreImages.some(p => src.includes(p)));
            
                        const data = {
                              content: postContent,
                              imageUrls: images,
                              cover: images[0] ?? ""
                        }
                       
                       setTimeout(()=>{
                            AndroidBridge?.sendData(JSON.stringify(data));
                            resolve([])
                       }, 500)                                     
                   
               } catch(ex){
                    console.log("ex", ex)
               }
           })
         })();
    """.trimIndent()
)

data class ScrapedData(
    val content: String = "",
    var cover: String = "",
    val imageUrls: List<String> = emptyList()
)

class JSBridge(
    private val onSuccess: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    @JavascriptInterface
    fun sendData(data: String) {
        onSuccess(data)
    }

    @JavascriptInterface
    fun sendError(error: String) {
        onError(error)
    }
}

@SuppressLint("SetJavaScriptEnabled", "RememberReturnType")
@Composable
fun Crawler(url: String?, onCompleted: (data: ScrapedData) -> Unit) {
    val context = LocalContext.current
    var scrapingCompleted by remember { mutableStateOf(false) }
    var webViewError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewInitialized by remember { mutableStateOf(false) }

    val currentOnCompleted by rememberUpdatedState(onCompleted)

    val handler = remember { Handler(Looper.getMainLooper()) }
    val retryCount = remember { mutableStateOf(0) }
    val maxRetries = 3

    val scriptText = remember(url) {
        when {
            url?.contains("facebook.com") == true -> script["facebook"].toString()
            url?.contains("linkedin.com") == true -> script["linkedin"].toString()
            else -> ""
        }
    }

    // Safe WebView initialization
    val webView = remember {
        try {
            // Initialize WebView on the main thread
            handler.post {
                try {
                    val webViewInstance = WebView(context).apply {
                        with(settings) {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            javaScriptCanOpenWindowsAutomatically = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            allowContentAccess = true
                            allowFileAccess = true

                            // Additional settings for better compatibility
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mixedContentMode = 0 // MIXED_CONTENT_ALWAYS_ALLOW
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, pageUrl: String?) {
                                super.onPageFinished(view, pageUrl)
                                isLoading = false

                                handler.postDelayed({
                                    if (!scrapingCompleted && retryCount.value < maxRetries) {
                                        try {
                                            evaluateJavascript(scriptText) { result ->
                                                Log.d("Crawler", "JavaScript execution result: $result")
                                            }
                                            retryCount.value++
                                        } catch (e: Exception) {
                                            Log.e("Crawler", "JavaScript evaluation failed", e)
                                        }
                                    }
                                }, 2000)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                webViewError = "WebView Error: $description (Code: $errorCode)"
                                isLoading = false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            // Can be used to handle console messages if needed
                        }

                        addJavascriptInterface(
                            JSBridge(
                                onSuccess = { data ->
                                    handler.post {
                                        try {
                                            val gson = Gson()
                                            val scrapedData = gson.fromJson(data, ScrapedData::class.java)
                                            scrapingCompleted = true
                                            currentOnCompleted(scrapedData)
                                        } catch (e: Exception) {
                                            webViewError = "Failed to parse scraped data: ${e.message}"
                                            Log.e("Crawler", "Error parsing data", e)
                                        }
                                    }
                                },
                                onError = { error ->
                                    handler.post {
                                        webViewError = "JavaScript Error: $error"
                                        Log.e("Crawler", "JavaScript error: $error")
                                    }
                                }
                            ),
                            "AndroidBridge"
                        )
                    }
                    webViewInitialized = true
                } catch (e: Exception) {
                    Log.e("Crawler", "WebView initialization failed", e)
                    webViewError = "WebView initialization failed: ${e.message}"
                    webViewInitialized = false
                    isLoading = false
                }
            }
            null // Return null initially, we'll handle this in the UI
        } catch (e: Exception) {
            Log.e("Crawler", "WebView creation failed", e)
            webViewError = "WebView creation failed: ${e.message}"
            null
        }
    }

    // Load URL when composable is launched or URL changes
    LaunchedEffect(url, webViewInitialized) {
        if (!url.isNullOrBlank() && webViewInitialized) {
            scrapingCompleted = false
            webViewError = null
            retryCount.value = 0
            isLoading = true

            try {
                // This would need to be handled differently since webView might be null
                // In a real implementation, you'd use a callback or state flow
            } catch (e: Exception) {
                webViewError = "Failed to load URL: ${e.message}"
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when {
            !webViewInitialized && isLoading -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Initializing WebView...",
                        fontSize = 14.sp
                    )
                }
            }

            webViewError != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WebView Error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = webViewError ?: "Unknown error",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Web scraping is not available on this device",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (retryCount.value < maxRetries) {
                        Button(
                            onClick = {
                                webViewError = null
                                isLoading = true
                                // You might want to implement a proper retry mechanism
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )
                }

                // Only show WebView if it's properly initialized
                if (webViewInitialized) {
                    AndroidView(
                        factory = { context ->
                            try {
                                WebView(context).apply {
                                    with(settings) {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        javaScriptCanOpenWindowsAutomatically = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        allowContentAccess = true
                                        allowFileAccess = true
                                    }

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, pageUrl: String?) {
                                            isLoading = false
                                            handler.postDelayed({
                                                evaluateJavascript(scriptText) { }
                                            }, 2000)
                                        }
                                    }

                                    addJavascriptInterface(
                                        JSBridge(
                                            onSuccess = { data ->
                                                handler.post {
                                                    try {
                                                        val scrapedData = Gson().fromJson(data, ScrapedData::class.java)
                                                        scrapingCompleted = true
                                                        currentOnCompleted(scrapedData)
                                                    } catch (e: Exception) {
                                                        webViewError = "Data parsing error: ${e.message}"
                                                    }
                                                }
                                            },
                                            onError = { error ->
                                                handler.post { webViewError = "JS Error: $error" }
                                            }
                                        ),
                                        "AndroidBridge"
                                    )

                                    url?.let { loadUrl(it) }
                                }
                            } catch (e: Exception) {
                                // Fallback to a simple message if WebView fails
                                webViewError = "WebView not available: ${e.message}"
                                WebView(context) // Return empty WebView
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}