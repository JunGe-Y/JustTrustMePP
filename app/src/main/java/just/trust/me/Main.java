package just.trust.me;

import android.content.Context;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpParams;

public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "JustTrustMe";
    String currentPackageName = "";

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        this.currentPackageName = loadPackageParam.packageName;
        Log.d(TAG, "Hooking SSLSocketFactory(String, KeyStore, String, KeyStore) for: " + this.currentPackageName);
        XposedHelpers.findAndHookConstructor(SSLSocketFactory.class, new Object[]{String.class, KeyStore.class, String.class, KeyStore.class, SecureRandom.class, HostNameResolver.class, new XC_MethodHook() {
            /* class just.trust.me.Main.1 */

            /* access modifiers changed from: protected */
            public void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                KeyManager[] keyManagerArr;
                String str = (String) methodHookParam.args[0];
                KeyStore keyStore = (KeyStore) methodHookParam.args[1];
                String str2 = (String) methodHookParam.args[2];
                SecureRandom secureRandom = (SecureRandom) methodHookParam.args[4];
                if (keyStore != null) {
                    keyManagerArr = (KeyManager[]) XposedHelpers.callStaticMethod(SSLSocketFactory.class, "createKeyManagers", new Object[]{keyStore, str2});
                } else {
                    keyManagerArr = null;
                }
                TrustManager[] trustManagerArr = {new ImSureItsLegitTrustManager()};
                XposedHelpers.setObjectField(methodHookParam.thisObject, "sslcontext", SSLContext.getInstance(str));
                XposedHelpers.callMethod(XposedHelpers.getObjectField(methodHookParam.thisObject, "sslcontext"), "init", new Object[]{keyManagerArr, trustManagerArr, secureRandom});
                XposedHelpers.setObjectField(methodHookParam.thisObject, "socketfactory", XposedHelpers.callMethod(XposedHelpers.getObjectField(methodHookParam.thisObject, "sslcontext"), "getSocketFactory", new Object[0]));
            }
        }});
        Log.d(TAG, "Hooking static SSLSocketFactory(String, KeyStore, String, KeyStore) for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "getSocketFactory", new Object[]{new XC_MethodReplacement() {
            /* class just.trust.me.Main.2 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return (SSLSocketFactory) XposedHelpers.newInstance(SSLSocketFactory.class, new Object[0]);
            }
        }});
        Log.d(TAG, "Hooking SSLSocketFactory(Socket) for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "isSecure", new Object[]{Socket.class, new XC_MethodReplacement() {
            /* class just.trust.me.Main.3 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return true;
            }
        }});
        Log.d(TAG, "Hooking TrustManagerFactory.getTrustManagers() for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("javax.net.ssl.TrustManagerFactory", loadPackageParam.classLoader, "getTrustManagers", new Object[]{new XC_MethodHook() {
            /* class just.trust.me.Main.4 */

            /* access modifiers changed from: protected */
            public void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if (Main.this.hasTrustManagerImpl()) {
                    Class findClass = XposedHelpers.findClass("com.android.org.conscrypt.TrustManagerImpl", loadPackageParam.classLoader);
                    TrustManager[] trustManagerArr = (TrustManager[]) methodHookParam.getResult();
                    if (trustManagerArr.length > 0 && findClass.isInstance(trustManagerArr[0])) {
                        return;
                    }
                }
                methodHookParam.setResult(new TrustManager[]{new ImSureItsLegitTrustManager()});
            }
        }});
        Log.d(TAG, "Hooking HttpsURLConnection.setDefaultHostnameVerifier for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("javax.net.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setDefaultHostnameVerifier", new Object[]{HostnameVerifier.class, new XC_MethodReplacement() {
            /* class just.trust.me.Main.5 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        Log.d(TAG, "Hooking HttpsURLConnection.setSSLSocketFactory for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("javax.net.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setSSLSocketFactory", new Object[]{javax.net.ssl.SSLSocketFactory.class, new XC_MethodReplacement() {
            /* class just.trust.me.Main.6 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        Log.d(TAG, "Hooking HttpsURLConnection.setHostnameVerifier for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("javax.net.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setHostnameVerifier", new Object[]{HostnameVerifier.class, new XC_MethodReplacement() {
            /* class just.trust.me.Main.7 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        Log.d(TAG, "Hooking WebViewClient.onReceivedSslError(WebView, SslErrorHandler, SslError) for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("android.webkit.WebViewClient", loadPackageParam.classLoader, "onReceivedSslError", new Object[]{WebView.class, SslErrorHandler.class, SslError.class, new XC_MethodReplacement() {
            /* class just.trust.me.Main.8 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                ((SslErrorHandler) methodHookParam.args[1]).proceed();
                return null;
            }
        }});
        Log.d(TAG, "Hooking WebViewClient.onReceivedSslError(WebView, int, string, string) for: " + this.currentPackageName);
        XposedHelpers.findAndHookMethod("android.webkit.WebViewClient", loadPackageParam.classLoader, "onReceivedError", new Object[]{WebView.class, Integer.TYPE, String.class, String.class, new XC_MethodReplacement() {
            /* class just.trust.me.Main.9 */

            /* access modifiers changed from: protected */
            public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        }});
        XposedHelpers.findAndHookMethod("javax.net.ssl.SSLContext", loadPackageParam.classLoader, "init", new Object[]{KeyManager[].class, TrustManager[].class, SecureRandom.class, new XC_MethodHook() {
            /* class just.trust.me.Main.10 */

            /* access modifiers changed from: protected */
            public void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                methodHookParam.args[0] = null;
                methodHookParam.args[1] = new TrustManager[]{new ImSureItsLegitTrustManager()};
                methodHookParam.args[2] = null;
            }
        }});
        XposedHelpers.findAndHookMethod("android.app.Application", loadPackageParam.classLoader, "attach", new Object[]{Context.class, new XC_MethodHook() {
            /* class just.trust.me.Main.11 */

            /* access modifiers changed from: protected */
            public void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Context context = (Context) methodHookParam.args[0];
                Main.this.processOkHttp(context.getClassLoader());
                Main.this.processHttpClientAndroidLib(context.getClassLoader());
                Main.this.processXutils(context.getClassLoader());
            }
        }});
        if (hasTrustManagerImpl()) {
            Log.d(TAG, "Hooking com.android.org.conscrypt.TrustManagerImpl for: " + this.currentPackageName);
            XposedHelpers.findAndHookMethod("com.android.org.conscrypt.TrustManagerImpl", loadPackageParam.classLoader, "checkServerTrusted", new Object[]{X509Certificate[].class, String.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.12 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return 0;
                }
            }});
            XposedHelpers.findAndHookMethod("com.android.org.conscrypt.TrustManagerImpl", loadPackageParam.classLoader, "checkServerTrusted", new Object[]{X509Certificate[].class, String.class, String.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.13 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return new ArrayList();
                }
            }});
            XposedHelpers.findAndHookMethod("com.android.org.conscrypt.TrustManagerImpl", loadPackageParam.classLoader, "checkServerTrusted", new Object[]{X509Certificate[].class, String.class, SSLSession.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.14 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return new ArrayList();
                }
            }});
        }
    }

    public boolean hasTrustManagerImpl() {
        try {
            Class.forName("com.android.org.conscrypt.TrustManagerImpl");
            return true;
        } catch (ClassNotFoundException unused) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private javax.net.ssl.SSLSocketFactory getEmptySSLFactory() {
        try {
            SSLContext instance = SSLContext.getInstance("TLS");
            instance.init(null, new TrustManager[]{new ImSureItsLegitTrustManager()}, null);
            return instance.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException unused) {
            return null;
        }
    }



    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void processXutils(ClassLoader classLoader) {
        Log.d(TAG, "Hooking org.xutils.http.RequestParams.setSslSocketFactory(SSLSocketFactory) (3) for: " + this.currentPackageName);
        try {
            classLoader.loadClass("org.xutils.http.RequestParams");
            XposedHelpers.findAndHookMethod("org.xutils.http.RequestParams", classLoader, "setSslSocketFactory", javax.net.ssl.SSLSocketFactory.class, new XC_MethodHook() {
                public void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    methodHookParam.args[0] = Main.this.getEmptySSLFactory();
                }
            });
            XposedHelpers.findAndHookMethod("org.xutils.http.RequestParams", classLoader, "setHostnameVerifier", new Object[]{HostnameVerifier.class, new XC_MethodHook() {
                /* class just.trust.me.Main.19 */

                /* access modifiers changed from: protected */
                public void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    methodHookParam.args[0] = new ImSureItsLegitHostnameVerifier();
                }
            }});
        } catch (Exception unused) {
            Log.d(TAG, "org.xutils.http.RequestParams not found in " + this.currentPackageName + "-- not hooking");
        }
    }

    /* access modifiers changed from: package-private */
    public void processOkHttp(ClassLoader classLoader) {
        Log.d(TAG, "Hooking com.squareup.okhttp.CertificatePinner.check(String,List) (2.5) for: " + this.currentPackageName);
        try {
            classLoader.loadClass("com.squareup.okhttp.CertificatePinner");
            XposedHelpers.findAndHookMethod("com.squareup.okhttp.CertificatePinner", classLoader, "check", new Object[]{String.class, List.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.20 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return true;
                }
            }});
        } catch (ClassNotFoundException unused) {
            Log.d(TAG, "OKHTTP 2.5 not found in " + this.currentPackageName + "-- not hooking");
        }
        Log.d(TAG, "Hooking okhttp3.CertificatePinner.check(String,List) (3.x) for: " + this.currentPackageName);
        try {
            classLoader.loadClass("okhttp3.CertificatePinner");
            XposedHelpers.findAndHookMethod("okhttp3.CertificatePinner", classLoader, "check", new Object[]{String.class, List.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.21 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return null;
                }
            }});
        } catch (ClassNotFoundException unused2) {
            Log.d(TAG, "OKHTTP 3.x not found in " + this.currentPackageName + " -- not hooking");
        }
        try {
            classLoader.loadClass("okhttp3.internal.tls.OkHostnameVerifier");
            XposedHelpers.findAndHookMethod("okhttp3.internal.tls.OkHostnameVerifier", classLoader, "verify", new Object[]{String.class, SSLSession.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.22 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return true;
                }
            }});
        } catch (ClassNotFoundException unused3) {
            Log.d(TAG, "OKHTTP 3.x not found in " + this.currentPackageName + " -- not hooking OkHostnameVerifier.verify(String, SSLSession)");
        }
        try {
            classLoader.loadClass("okhttp3.internal.tls.OkHostnameVerifier");
            XposedHelpers.findAndHookMethod("okhttp3.internal.tls.OkHostnameVerifier", classLoader, "verify", new Object[]{String.class, X509Certificate.class, new XC_MethodReplacement() {
                /* class just.trust.me.Main.23 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return true;
                }
            }});
        } catch (ClassNotFoundException unused4) {
            Log.d(TAG, "OKHTTP 3.x not found in " + this.currentPackageName + " -- not hooking OkHostnameVerifier.verify(String, X509)(");
        }
    }

    /* access modifiers changed from: package-private */
    public void processHttpClientAndroidLib(ClassLoader classLoader) {
        Log.d(TAG, "Hooking AbstractVerifier.verify(String, String[], String[], boolean) for: " + this.currentPackageName);
        try {
            classLoader.loadClass("ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier");
            XposedHelpers.findAndHookMethod("ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier", classLoader, "verify", new Object[]{String.class, String[].class, String[].class, Boolean.TYPE, new XC_MethodReplacement() {
                /* class just.trust.me.Main.24 */

                /* access modifiers changed from: protected */
                public Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return null;
                }
            }});
        } catch (ClassNotFoundException unused) {
            Log.d(TAG, "httpclientandroidlib not found in " + this.currentPackageName + "-- not hooking");
        }
    }

    /* access modifiers changed from: private */
    public class ImSureItsLegitTrustManager implements X509TrustManager {
        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        }

        private ImSureItsLegitTrustManager() {
        }

        public List<X509Certificate> checkServerTrusted(X509Certificate[] x509CertificateArr, String str, String str2) throws CertificateException {
            return new ArrayList();
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private class ImSureItsLegitHostnameVerifier implements HostnameVerifier {
        public boolean verify(String str, SSLSession sSLSession) {
            return true;
        }

        private ImSureItsLegitHostnameVerifier() {
        }
    }

    public class TrustAllSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public TrustAllSSLSocketFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(keyStore);
            X509TrustManager r4 = new X509TrustManager() {
                /* class just.trust.me.Main.TrustAllSSLSocketFactory.1 */

                @Override // javax.net.ssl.X509TrustManager
                public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
                }

                @Override // javax.net.ssl.X509TrustManager
                public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            this.sslContext.init(null, new TrustManager[]{r4}, null);
        }

        @Override // org.apache.http.conn.scheme.LayeredSocketFactory, org.apache.http.conn.ssl.SSLSocketFactory
        public Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException, UnknownHostException {
            return this.sslContext.getSocketFactory().createSocket(socket, str, i, z);
        }

        @Override // org.apache.http.conn.scheme.SocketFactory, org.apache.http.conn.ssl.SSLSocketFactory
        public Socket createSocket() throws IOException {
            return this.sslContext.getSocketFactory().createSocket();
        }
    }
}
