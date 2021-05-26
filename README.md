JustTrustMe++ By:军哥
===================


## JustTrustMe介绍

如果您对JustTrustMe已经有足够的了解，可跳过该介绍

需要捕获应用网络流量信息的时候(简称抓包)，若应用采用是Https加密传输，便很有可能进行SSL证书验证，使你无法进行抓包，而JustTrustMe模块就是通过Xposed对应用进行Hook，使目标应用无法进行SSL验证，以达到顺利抓包;

## 前言
对于使用了Okhttp且被被混淆的app，JustTrustMe将会失去部分效果，因为JustTrustMe将找不到okhttp的相关类，所以我们需要一个能够全自动识别混淆后okhttp类名的方式
看了"珍惜"大佬的文章[https://www.jianshu.com/p/6f15720c7155](https://www.jianshu.com/p/6f15720c7155),已经实现了该方式
## 识别思路
根据okhttp的类特征进行对比，比如某个类有几个构造函数，构造函数有几个参数，构造函数的参数类型分别是哪些，类成员有几个，类成员类型有哪些，每个对应的类型有多少个
## 现成解决方案
"珍惜"大佬的解决方案是：应用在启动后，遍历dex中的所有类，并逐个对比识别
## 现成解决方案弊端
1、某些应用的体积较大，类较多，需要遍历的类数量庞大，性能降低，甚至卡死

2、若采用线程识别的方式，对于应用体积大的app，可以避免卡顿，但异步识别，也将带来一个弊端：对于app刚启动，便需要抓app初始化请求的时候，很可能插件尚未定位到okhttp，无法hook，导致无法抓包

3、需要做hook配置，保存需要hook的包名，否则在系统重启的时候，较多系统应用启动，需要搜索较多的类，导致系统启动迟钝
## 处理方案
在这里，我的思路是，hook那些日常开发中很少用到的类，但okhttp在验证证书之前又必须用到的类，然后通过这个类的调用堆栈，去识别okhttp的相关类，这么一来，可以极为有效的避免那些无关类，且支持直接hook所有app，因为如果应用没有用到该类，便不会进行识别。

而通过一些分析，我确认要hook的类就是OpenSSLSocketFactoryImpl,在OpenSSLSocketFactoryImpl的createSocket调用堆栈中，可以找到RealConnection类,通过RealConnection类可以找到CertificatePinner类和OkHostnameVerifier类。

Hook找到的CertificatePinner类的和OkHostnameVerifier类，便可以跳过证书验证

## 调用堆栈如下：
```
    java.lang.Throwable
         at com.junge.JustTrustMePP.Hook$1.beforeHookedMethod(Hook.java:41)
         at de.robv.android.xposed.XposedBridge.handleHookedMethod(XposedBridge.java:340)
         at com.android.org.conscrypt.OpenSSLSocketFactoryImpl.createSocket(<Xposed>)
         at okhttp3.internal.connection.RealConnection.connectTls(RealConnection.java:325)
         at okhttp3.internal.connection.RealConnection.establishProtocol(RealConnection.java:300)
         at okhttp3.internal.connection.RealConnection.connect(RealConnection.java:185)
         at okhttp3.internal.connection.ExchangeFinder.findConnection(ExchangeFinder.java:224)
         at okhttp3.internal.connection.ExchangeFinder.findHealthyConnection(ExchangeFinder.java:107)
         at okhttp3.internal.connection.ExchangeFinder.find(ExchangeFinder.java:87)
         at okhttp3.internal.connection.Transmitter.newExchange(Transmitter.java:169)
         at okhttp3.internal.connection.ConnectInterceptor.intercept(ConnectInterceptor.java:41)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:142)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:117)
         at okhttp3.internal.cache.CacheInterceptor.intercept(CacheInterceptor.java:94)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:142)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:117)
         at okhttp3.internal.http.BridgeInterceptor.intercept(BridgeInterceptor.java:93)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:142)
         at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(RetryAndFollowUpInterceptor.java:88)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:142)
         at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.java:117)
         at okhttp3.RealCall.getResponseWithInterceptorChain(RealCall.java:221)
         at okhttp3.RealCall.execute(RealCall.java:81)
         at com.example.myapplication.MainActivity.lambda$onClick$1$MainActivity(MainActivity.java:51)
         at com.example.myapplication.-$$Lambda$MainActivity$0SzM7AHmmjb26RoY3d_i5JLsysk.run(lambda)
         at java.lang.Thread.run(Thread.java:761)
```
## 样例文件
如果您想要一个混淆后的okhttp进行测试，它就在根目录，您可以直接下载

如果您不想编译，想直接使用，[请点击这里](https://github.com/JunGe-Y/JustTrustMePP/tree/master/app/release)
## 联系方式
如果您在使用过程中遇到问题，可与我联系！

QQ：757456456

email：757456456@qq.com

QQ交流群：1064330788
