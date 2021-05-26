package com.junge.JustTrustMePP;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook extends just.trust.me.Main{
    private static final String TAG = "JustTrustMePp";
    public Class CertificatePinner=null;
    public Class RealConnection=null;
    public Class OkHostnameVerifier=null;
    public  HashMap<String,Boolean> searched=new HashMap<String,Boolean> ();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final ClassLoader classLoader=loadPackageParam.classLoader;
        try{
            /*
                对于被okhttp被混淆的app，JustTrustMe将会失去效果，而通过混淆类的自动定位，可以找出被混淆的okhttp
                类的自动定位思路就是，根据okhttp的类特征进行对比，比如有几个构造函数，构造函数有几个参数，构造函数的参数分别是哪些，类成员有哪些
                对比的前提是你要拿到可疑的类，方法有很多，比如说，在app加载的时候，遍历dex的所有类，但这种明显效率并不高
                在这里，我的思路是，hook那些日常开发中很少用到的类，但okhttp又必须用到的类，然后通过这个类的调用堆栈，去寻找okhttp的相关类
                而我通过一些分析，确认要hook的类就是OpenSSLSocketFactoryImpl
                在OpenSSLSocketFactoryImpl的createSocket调用堆栈中，可以找到RealConnection类,通过RealConnection类可以找到CertificatePinner类和OkHostnameVerifier类
                Hook找到CertificatePinner类的和OkHostnameVerifier类，便可以跳过证书验证
             */
            Class OpenSSLSocketFactoryImpl=classLoader.loadClass("com.android.org.conscrypt.OpenSSLSocketFactoryImpl");
            XposedBridge.hookAllMethods(OpenSSLSocketFactoryImpl, "createSocket", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(CertificatePinner==null){
                        Throwable throwable=new Throwable();
                        throwable.printStackTrace();
                        StackTraceElement[] stackTraceElements=throwable.getStackTrace();

                        //遍历调用堆栈信息
                        for(StackTraceElement stackTraceElement:stackTraceElements) {
                            try{
                                //拿到堆栈类名
                                String className= stackTraceElement.getClassName();

                                //已搜索列表不存在该类才会进行搜索
                                if(!searched.containsKey(className) || !searched.get(className)){
                                    //将该类加入已搜索列表，避免重复搜索，导致性能降低
                                    searched.put(className,true);
                                    Class thisObj=classLoader.loadClass(className);
                                    Field[] fields = thisObj.getDeclaredFields();
                                    String superName=thisObj.getSuperclass().getName();

                                    //RealConnection的父类非object
                                    if(superName.equals("java.lang.Object")){
                                        continue;
                                    }
                                    int Class_list_Num=0;
                                    int Class_socket_Num=0;
                                    int Class_boolean_Num=0;
                                    int Class_long_Num=0;
                                    int Class_int_Num=0;

                                    //遍历该类的所有成员
                                    for (Field field : fields) {
                                        String fieldType=field.getType().getName();
                                        boolean isStatic=Modifier.isStatic(field.getModifiers());
                                        boolean isFinal=Modifier.isFinal(field.getModifiers());
                                        if(fieldType.equals("java.util.List") && !isStatic && isFinal ){
                                            Class_list_Num++;
                                        }
                                        if(fieldType.equals("java.net.Socket") && !isStatic && !isFinal ){
                                            Class_socket_Num++;
                                        }
                                        if(fieldType.equals("boolean") && !isStatic && !isFinal ){
                                            Class_boolean_Num++;
                                        }
                                        if(fieldType.equals("long") && !isStatic && !isFinal ){
                                            Class_long_Num++;
                                        }
                                        if(fieldType.equals("int") && !isStatic && !isFinal ){
                                            Class_int_Num++;
                                        }
                                    }

                                    //RealConnection有4个非静态int，1个fincal list，2个socket，1个boolean，1个long，
                                    if(Class_int_Num == 4 && Class_list_Num ==1 && Class_socket_Num==2 && Class_boolean_Num==1 && Class_long_Num==1){
                                        RealConnection= thisObj;
                                        foundRealConnection();
                                        break;
                                    }
                                }
                            }catch (Exception e){}
                        }
                    }
                    super.beforeHookedMethod(param);
                }

            });
        }catch (Exception e){}


        super.handleLoadPackage(loadPackageParam);
    }
    public void foundRealConnection(){
        Log.d(TAG,"找到RealConnection类"+RealConnection.getName());
        Constructor[] cons=RealConnection.getDeclaredConstructors();
        //只有一个构造函数
        if(cons.length ==1){
            Class[] parameterTypes=cons[0].getParameterTypes();
            //构造函数只有2个参数,第二个参数就是Route
            if(parameterTypes.length==2){
                Class Route=parameterTypes[1];

                founRoute(Route);
            }
        }
        foundCertificatePinner();
    }

    public void founRoute(Class Route){
        Log.d(TAG,"找到Route类"+Route.getName());
        Constructor[] cons=Route.getDeclaredConstructors();
        //Route只有一个构造函数
        if(cons.length ==1){
            Class[] parameterTypes=cons[0].getParameterTypes();
            //构造函数有3个参数，第二个参数就是Address
            if(parameterTypes.length==3){
                Class Address=parameterTypes[0];
                founAddress(Address);
            }
        }
    }

    public void founAddress(Class Address){
        Log.d(TAG,"找到Address类"+ Address.getName());
        Constructor[] cons= Address.getDeclaredConstructors();
        //Address只有一个构造函数
        if(cons.length ==1){
            Class[] parameterTypes=cons[0].getParameterTypes();
            //构造函数有12个参数，第6个参数是OkHostnameVerifier，但是声明是接口类，只能通过hook拿实例类，第7个参数就是CertificatePinner,
            if(parameterTypes.length==12){

                CertificatePinner=parameterTypes[6];
                foundCertificatePinner();
            }
            XposedBridge.hookAllConstructors(Address, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(OkHostnameVerifier==null){
                        OkHostnameVerifier=param.args[5].getClass();
                        foundOkHostnameVerifier();
                    }

                    super.beforeHookedMethod(param);
                }
            });
        }
    }

    public void foundOkHostnameVerifier(){
        Log.d(TAG,"找到OkHostnameVerifier类"+ OkHostnameVerifier.getName());
        hookOkHostnameVerifier(OkHostnameVerifier);
    }
    public void hookOkHostnameVerifier(Class object) {
        XposedBridge.hookAllMethods(object, "verify", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return true;
            }
        });
    }


    public void foundCertificatePinner(){
        Log.d(TAG,"找到CertificatePinner类"+ CertificatePinner.getName());
        hookCertificatePinner(CertificatePinner);
    }
    public void hookCertificatePinner(Class object) {
        Method[] ms = object.getDeclaredMethods();
        Method hookMehod=null;

        //遍历所有函数，我们要Hook的函数参数类型就是String,List

        for(Method method:ms){
            Class<?> returnType = method.getReturnType();
            if(!returnType.getName().equals("void")){
                continue;
            }
            Class[] paramTypes = method.getParameterTypes();
            if(paramTypes.length!=2){
                continue;
            }
            if(!paramTypes[0].getName().equals("java.lang.String")){
                continue;
            }
            if(!paramTypes[1].getName().equals("java.util.List")){
                continue;
            }
            hookMehod=method;
        }
        if(hookMehod!=null){
            XposedBridge.hookMethod(hookMehod, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return null;
                }
            });
        }
    }
}
