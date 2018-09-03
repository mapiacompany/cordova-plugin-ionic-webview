package com.ionicframework.cordova.webview;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.app.AlertDialog;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Toast;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;

import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.cordova.engine.SystemWebView;

import java.net.URISyntaxException;
import java.util.Hashtable;

public class IonicWebViewEngine extends SystemWebViewEngine {
    public static final String TAG = "IonicWebViewEngine";

    private WebViewLocalServer localServer;
    private String CDV_LOCAL_SERVER;

    /**
     * Used when created via reflection.
     */
    public IonicWebViewEngine(Context context, CordovaPreferences preferences) {
        super(new SystemWebView(context), preferences);
        Log.d(TAG, "Ionic Web View Engine Starting Right Up 1...");
    }

    public IonicWebViewEngine(SystemWebView webView) {
        super(webView, null);
        Log.d(TAG, "Ionic Web View Engine Starting Right Up 2...");
    }

    public IonicWebViewEngine(SystemWebView webView, CordovaPreferences preferences) {
        super(webView, preferences);
        Log.d(TAG, "Ionic Web View Engine Starting Right Up 3...");
    }

    @Override
    public void init(CordovaWebView parentWebView, CordovaInterface cordova, final CordovaWebViewEngine.Client client,
                     CordovaResourceApi resourceApi, PluginManager pluginManager,
                     NativeToJsMessageQueue nativeToJsMessageQueue) {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(cordova.getActivity());

        String port = preferences.getString("WKPort", "8080");
        CDV_LOCAL_SERVER = "http://localhost:" + port;

        localServer = new WebViewLocalServer(cordova.getActivity(), "localhost:" + port, true, parser);
        WebViewLocalServer.AssetHostingDetails ahd = localServer.hostAssets("www");

        webView.setWebViewClient(new ServerClient(this, parser));

        super.init(parentWebView, cordova, client, resourceApi, pluginManager, nativeToJsMessageQueue);
    }


    private class ServerClient extends SystemWebViewClient {
        private ConfigXmlParser parser;

        private void getCardInstallAlertDialog(Context context, final String coCardNm) {

            final Hashtable<String, String> cardNm = new Hashtable<String, String>();
            cardNm.put("HYUNDAE", "현대 앱카드");
            cardNm.put("SAMSUNG", "삼성 앱카드");
            cardNm.put("LOTTE", "롯데 앱카드");
            cardNm.put("SHINHAN", "신한 앱카드");
            cardNm.put("KB", "국민 앱카드");
            cardNm.put("HANASK", "하나SK 통합안심클릭");
            //cardNm.put("SHINHAN_SMART",  "Smart 신한앱");

            final Hashtable<String, String> cardInstallUrl = new Hashtable<String, String>();
            cardInstallUrl.put("HYUNDAE", "market://details?id=com.hyundaicard.appcard");
            cardInstallUrl.put("SAMSUNG", "market://details?id=kr.co.samsungcard.mpocket");
            cardInstallUrl.put("LOTTE", "market://details?id=com.lotte.lottesmartpay");
            cardInstallUrl.put("SHINHAN", "market://details?id=com.shcard.smartpay");
            cardInstallUrl.put("KB", "market://details?id=com.kbcard.cxh.appcard");
            cardInstallUrl.put("HANASK", "market://details?id=com.ilk.visa3d");
            //cardInstallUrl.put("SHINHAN_SMART",  "market://details?id=com.shcard.smartpay");//여기 수정 필요!!2014.04.01

            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("알림")
                    .setMessage(cardNm.get(coCardNm) + " 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                    .setPositiveButton("설치", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String installUrl = cardInstallUrl.get(coCardNm);
                            Uri uri = Uri.parse(installUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            Log.d("<INIPAYMOBILE>", "Call : " + uri.toString());
                            try {
                                context.startActivity(intent);
                            } catch (ActivityNotFoundException anfe) {
                                Toast.makeText(context, cardNm.get(coCardNm) + "설치 url이 올바르지 않습니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(context, "(-1)결제를 취소 하셨습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }).create().show();

        }

        private void showISPDialog(Context context) {//ShowDialog
            Dialog alertIsp = new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("알림")
                    .setMessage("모바일 ISP 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                    .setPositiveButton("설치", (dialog1, which) -> {
                        String ispUrl = "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ispUrl));
                        context.startActivity(intent);
                    })
                    .setNegativeButton("취소", (dialog12, which) -> {
                        Toast.makeText(context, "(-1)결제를 취소 하셨습니다.", Toast.LENGTH_SHORT).show();
                        // @TODO finish();
                    })
                    .create();

            alertIsp.show();

        }//end onCreateDialog


        public ServerClient(SystemWebViewEngine parentEngine, ConfigXmlParser parser) {
            super(parentEngine);
            this.parser = parser;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            /*
             * URL별로 분기가 필요합니다. 어플리케이션을 로딩하는것과
             * WEB PAGE를 로딩하는것을 분리 하여 처리해야 합니다.
             * 만일 가맹점 특정 어플 URL이 들어온다면
             * 조건을 더 추가하여 처리해 주십시요.
             */

            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:") && !url.startsWith("file:")) {
                Intent intent;

                try {
                    Log.d("<INIPAYMOBILE>", "intent url : " + url);
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                    Log.d("<INIPAYMOBILE>", "intent getDataString : " + intent.getDataString());
                    Log.d("<INIPAYMOBILE>", "intent getPackage : " + intent.getPackage());

                } catch (URISyntaxException ex) {
                    Log.e("<INIPAYMOBILE>", "URI syntax error : " + url + ":" + ex.getMessage());
                    return false;
                }

                Uri uri = Uri.parse(intent.getDataString());
                intent = new Intent(Intent.ACTION_VIEW, uri);

                try {

                    view.getContext().startActivity(intent);

                    /*가맹점의 사정에 따라 현재 화면을 종료하지 않아도 됩니다.
                        삼성카드 기타 안심클릭에서는 종료되면 안되기 때문에
                        조건을 걸어 종료하도록 하였습니다.*/
                    if (url.startsWith("ispmobile://")) {
                        return true;
                    }

                } catch (ActivityNotFoundException e) {
                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, ActivityNotFoundException INPUT >> " + url);
                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, uri.getScheme()" + intent.getDataString());

                    //ISP
                    if (url.startsWith("ispmobile://")) {
                        showISPDialog(view.getContext());
                        return true;
                    }

                    String data = intent.getDataString();
                    if (data == null) {
                        return super.shouldOverrideUrlLoading(view, url);
                    }

                    //현대앱카드
                    if (data.startsWith("hdcardappcardansimclick://")) {
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 현대앱카드설치 ");
                        getCardInstallAlertDialog(view.getContext(), "HYUNDAE");
                        return true;
                    }

                    //신한앱카드
                    else if (data.startsWith("shinhan-sr-ansimclick://")) {
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 신한카드앱설치 ");
                        getCardInstallAlertDialog(view.getContext(), "SHINHAN");
                        return true;
                    }

                    //삼성앱카드
                    else if (data.startsWith("mpocket.online.ansimclick://")) {
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 삼성카드앱설치 ");
                        getCardInstallAlertDialog(view.getContext(), "SAMSUNG");
                        return true;
                    }

                    //롯데앱카드
                    else if (data.startsWith("lottesmartpay://")) {
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 롯데카드앱설치 ");
                        getCardInstallAlertDialog(view.getContext(), "LOTTE");
                        return true;
                    }

                    //KB앱카드
                    else if (data.startsWith("kb-acp://")) {
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, KB카드앱설치 ");
                        getCardInstallAlertDialog(view.getContext(), "KB");
                        return true;
                    }

                    //하나SK카드 통합안심클릭앱
                    else if (data.startsWith("hanaansim://")) {
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 하나카드앱설치 ");
                        getCardInstallAlertDialog(view.getContext(), "HANASK");
                        return true;
                    }

                    /*
                    //신한카드 SMART신한 앱
                    else if( intent.getDataString().startsWith("smshinhanansimclick://"))
                    {
                     DIALOG_CARDNM = "SHINHAN_SMART";
                     Log.e("INIPAYMOBILE", "INIPAYMOBILE, Smart신한앱설치");
                     view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                     showDialog(DIALOG_CARDAPP);
                        return false;
                    }
                    */

                    /**
                     > 현대카드 안심클릭 droidxantivirusweb://
                     - 백신앱 : Droid-x 안드로이이드백신 - NSHC
                     - package name : net.nshc.droidxantivirus
                     - 특이사항 : 백신 설치 유무는 체크를 하고, 없을때 구글마켓으로 이동한다는 이벤트는 있지만, 구글마켓으로 이동되지는 않음
                     - 처리로직 : intent.getDataString()로 하여 droidxantivirusweb 값이 오면 현대카드 백신앱으로 인식하여
                     하드코딩된 마켓 URL로 이동하도록 한다.
                     */

                    //현대카드 백신앱
                    else if (data.startsWith("droidxantivirusweb")) {
                        /*************************************************************************************/
                        Log.d("<INIPAYMOBILE>", "ActivityNotFoundException, droidxantivirusweb 문자열로 인입될시 마켓으로 이동되는 예외 처리: ");
                        /*************************************************************************************/

                        Intent hydVIntent = new Intent(Intent.ACTION_VIEW);
                        hydVIntent.setData(Uri.parse("market://search?q=net.nshc.droidxantivirus"));
                        view.getContext().startActivity(hydVIntent);
                        return true;
                    }
                    //INTENT:// 인입될시 예외 처리
                    else if (url.startsWith("intent://")) {

                        /**

                         > 삼성카드 안심클릭
                         - 백신앱 : 웹백신 - 인프라웨어 테크놀러지
                         - package name : kr.co.shiftworks.vguardweb
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 신한카드 안심클릭
                         - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         - package name : com.TouchEn.mVaccine.webs
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 농협카드 안심클릭
                         - 백신앱 : V3 Mobile Plus 2.0
                         - package name : com.ahnlab.v3mobileplus
                         - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨

                         > 외환카드 안심클릭
                         - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         - package name : com.TouchEn.mVaccine.webs
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 씨티카드 안심클릭
                         - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         - package name : com.TouchEn.mVaccine.webs
                         - 특이사항 : INTENT:// 인입될시 정상적 호출

                         > 하나SK카드 안심클릭
                         - 백신앱 : V3 Mobile Plus 2.0
                         - package name : com.ahnlab.v3mobileplus
                         - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨

                         > 하나카드 안심클릭
                         - 백신앱 : V3 Mobile Plus 2.0
                         - package name : com.ahnlab.v3mobileplus
                         - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨

                         > 롯데카드
                         - 백신이 설치되어 있지 않아도, 결제페이지로 이동

                         */

                        /*************************************************************************************/
                        Log.d("<INIPAYMOBILE>", "Custom URL (intent://) 로 인입될시 마켓으로 이동되는 예외 처리: ");
                        /*************************************************************************************/

                        try {
                            Intent excepIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String packageNm = excepIntent.getPackage();

                            Log.d("<INIPAYMOBILE>", "excepIntent getPackage : " + packageNm);

                            excepIntent = new Intent(Intent.ACTION_VIEW);
                            excepIntent.setData(Uri.parse("market://search?q=" + packageNm));

                            view.getContext().startActivity(excepIntent);
                            return true;
                        } catch (URISyntaxException e1) {
                            Log.e("<INIPAYMOBILE>", "INTENT:// 인입될시 예외 처리  오류 : " + e1);
                        }

                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return localServer.shouldInterceptRequest(request.getUrl());
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return localServer.shouldInterceptRequest(Uri.parse(url));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url.equals(parser.getLaunchUrl())) {
                view.stopLoading();
                view.loadUrl(CDV_LOCAL_SERVER);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:(function() { " +
                    "window.WEBVIEW_SERVER_URL = '" + CDV_LOCAL_SERVER + "'" +
                    "})()");
        }
    }

    public void setServerBasePath(String path) {
        localServer.hostFiles(path);
        webView.loadUrl(CDV_LOCAL_SERVER);
    }

    public String getServerBasePath() {
        return this.localServer.getBasePath();
    }
}

