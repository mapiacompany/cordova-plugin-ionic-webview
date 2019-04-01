package com.ionicframework.cordova.webview;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import android.webkit.WebView;
import org.json.JSONArray;
import org.json.JSONException;

public class IonicWebView extends CordovaPlugin  {
  WebView mWebView;

  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
  
    if (action.equals("setServerBasePath")) {
      final String path = args.getString(0);
      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          ((IonicWebViewEngine)webView.getEngine()).setServerBasePath(path);
        }
      });
      return true;
    } else if (action.equals("getServerBasePath")) {
      callbackContext.success(((IonicWebViewEngine)webView.getEngine()).getServerBasePath());
    }
    return false;
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    if(mWebView != null){
      mWebView.onResume();
      mWebView.resumeTimers();
    }
  }

  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
    if(mWebView != null){
      mWebView.onPause();
      mWebView.pauseTimers();
    }
  }
}

