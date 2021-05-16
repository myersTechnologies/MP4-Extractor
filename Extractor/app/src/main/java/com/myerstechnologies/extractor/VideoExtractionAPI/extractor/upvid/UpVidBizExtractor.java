package com.myerstechnologies.extractor.VideoExtractionAPI.extractor.upvid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evgenii.jsevaluator.JsEvaluator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class UpVidBizExtractor {
    private Activity context;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private String url;
    private boolean isLoaded;
    private Thread worker;
    private  Timer timerObj;
    public UpVidBizExtractor(Activity context) {
        this.context = context;
    }

    public void extract(String myStreamLink)
    {
        this.url = myStreamLink;
        worker = new Thread(new Runnable(){

            @Override
            public void run() {
                if (worker.isInterrupted()){
                    Log.d("Interrupted", "Interrupted Task");
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getRealUrl(myStreamLink);
                    }
                });
            }
        });
        setTimeOut(url, 10000);
        worker.run();
    }
    private void setTimeOut(String link, int timeWait){

        timerObj = new Timer();
        TimerTask timerTaskObj = new TimerTask() {
            public void run() {
                //perform your action here
                if (!isLoaded){
                    extract(link);
                } else {
                    timerObj.cancel();
                }
            }
        };
        timerObj.schedule(timerTaskObj, timeWait, timeWait);

    }


    private String getRealUrl(String realUrlLink){
        final String[] realUrl = {realUrlLink};
        final boolean[] found = {false};
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    JsEvaluator jsEvaluator = new JsEvaluator(context);
                    WebView webView = jsEvaluator.getWebView();
                    webView.getSettings().setUserAgentString(USER_AGENT);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.getSettings().setSaveFormData(true);
                    webView.getSettings().setAllowContentAccess(true);
                    webView.getSettings().setAllowFileAccess(true);
                    webView.getSettings().setAllowFileAccessFromFileURLs(true);
                    webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    webView.getSettings().setSupportZoom(true);
                    webView.setClickable(true);
                    webView.setWebChromeClient(new WebChromeClient());
                    List<String> urls = new ArrayList<>();
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                            if(request.getUrl().toString().contains("pubdirecte")||request.getUrl().toString().contains("//pubdirecte.com/script/pop.php")
                                    ||request.getUrl().toString().contains("https://offerimage.com/www/")
                                    ||request.getUrl().toString().contains("https://littlecdn.com/contents/")
                                    || request.getUrl().toString().contains("h//cdn.betgorebysson.club/fac.php")
                                    || request.getUrl().toString().contains("https://ipp.littlecdn.com/web/static/play.png")
                                    || request.getUrl().toString().contains("https://o.wowreality.info")){
                                InputStream textStream = new ByteArrayInputStream("".getBytes());
                                return getTextWebResource(textStream);
                            } else {
                                if (request.getUrl().toString().contains(".mp4")){


                                }

                            }
                            Log.d("OOPOOOP", request.getUrl().toString());
                            if (request.getUrl().toString().contains("-_-")){
                                urls.add(request.getUrl().toString());
                            }

                            return super.shouldInterceptRequest(view, request);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            webView.loadUrl(urls.get(urls.size() -1));
                            Log.d("OOPOOOP565", urls.get(urls.size() -1));
                            //webView.loadUrl("about:blank");
                            //jsEvaluator.destroy();
                        }
                    });
                    webView.loadUrl(realUrl[0]);
                }
            });
        return realUrl[0];
    }

    private WebResourceResponse getTextWebResource(InputStream data) {
        return new WebResourceResponse("text/plain", "UTF-8", data);
    }

    private String getStreamMP4(){
        isLoaded = false;
        final String[] javascriptFile = {"nothing"};
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                JsEvaluator jsEvaluator = new JsEvaluator(context);
                WebView webView = jsEvaluator.getWebView();
                webView.getSettings().setUserAgentString(USER_AGENT);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setSaveFormData(true);
                webView.getSettings().setAllowContentAccess(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAllowFileAccessFromFileURLs(true);
                webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                webView.getSettings().setSupportZoom(true);
                webView.setClickable(true);
                webView.setWebChromeClient(new WebChromeClient());
                webView.setWebViewClient(new WebViewClient() {

                    @SuppressLint("JavascriptInterface")
                    @Override
                    public void onPageFinished(WebView view, String url1) {

                        super.onPageFinished(view, url1);
                        Log.d("finished", "FINISHED");
                        webView.evaluateJavascript(
                                "document.querySelector('.vjs-big-play-button').click();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        Log.d("CODE", html);
                                    }
                                });
                    }
                });
                webView.loadUrl(url);
            }
        });
        return javascriptFile[0];
    }
    protected abstract void onExtractionComplete(String mp4);

    protected abstract void onExtractionError(String link);
}
