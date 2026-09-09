package org.shareat.feature.profile.ui.terms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun TermsAndConditionsContent(modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration().apply {
                    preferences.setValue(false, forKey = "javaScriptEnabled")
                },
            ).apply {
                NSURL.URLWithString(TERMS_AND_CONDITIONS_URL)?.let { url ->
                    loadRequest(NSURLRequest.requestWithURL(url))
                }
            }
        },
    )
}
