package com.linmew.chatgpt

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import com.linmew.chatgpt.ui.theme.ChatgptTheme

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Chatgpt_Splash)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    setContent {
                        ChatgptTheme {
                            WebViewContainer(webView)
                        }
                    }
                }
            }
            settings.javaScriptEnabled = true
            loadUrl("https://chat.paimons.cn")
        }
        // 创建一个 OnBackPressedCallback 对象
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    // 当 WebView 无法回退时，禁用此回调，使事件可以向下分发
                    isEnabled = false
                    // 触发系统的返回处理
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }
}

@Composable
fun WebViewContainer(webView: WebView, modifier: Modifier = Modifier) {
    AndroidView({ webView }, modifier = modifier.fillMaxSize())
}