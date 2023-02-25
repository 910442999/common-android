# common-android
安卓简单框架及公共库，开箱即用，也可下载源码根据自己的情况进行修改

1、在你的项目gradle.properties 中添加
# Automatically convert third-party libraries to use AndroidX
android.enableJetifier=true
2、如果无法下载依赖则使用阿里云代理下载
maven { url 'https://maven.aliyun.com/repository/public' }
maven { url 'https://maven.aliyun.com/repository/google' }
maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
maven { url 'https://maven.aliyun.com/repository/releases' }