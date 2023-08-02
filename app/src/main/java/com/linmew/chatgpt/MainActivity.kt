package com.linmew.chatgpt

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.linmew.chatgpt.ui.theme.ChatgptTheme

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val requiredPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var chooseFileLauncher: ActivityResultLauncher<String>

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Chatgpt_Splash)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                // All permissions granted
            } else {
                Toast.makeText(this, "权限未授予", Toast.LENGTH_SHORT).show()
            }
        }

        chooseFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uploadMessage?.onReceiveValue(uri?.let { arrayOf(it) } ?: arrayOf())
            uploadMessage = null
        }

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
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                    if (!allPermissionsGranted()) {
                        requestPermissions()
                        return false
                    }
                    uploadMessage = filePathCallback
                    chooseFileLauncher.launch("*/*")
                    return true
                }
            }
            setDownloadListener { url, _, _, _, _ ->
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    // request permission
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                } else {
                    // permission already granted
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(request)
                }
            }

            loadUrl("https://chat.paimons.cn")
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(requiredPermissions)
    }

}

@Composable
fun WebViewContainer(webView: WebView, modifier: Modifier = Modifier) {
    AndroidView({ webView }, modifier = modifier.fillMaxSize())
}
