-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keep class com.yuanquan.common.widget.calendar.MonthView {
    public <init>(android.content.Context);
}
-keep class com.yuanquan.common.widget.calendar.WeekBar {
    public <init>(android.content.Context);
}
-keep class com.yuanquan.common.widget.calendar.WeekView {
    public <init>(android.content.Context);
}
-keep class com.yuanquan.common.widget.calendar.YearView {
    public <init>(android.content.Context);
}

# 保持ViewModel和ViewBinding不混淆，否则无法反射自动创建
-keep class * implements androidx.viewbinding.ViewBinding { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

-keep class com.luck.picture.lib.** { *; }

# 如果引入了Camerax库请添加混淆
-keep class com.luck.lib.camerax.** { *; }

# 如果引入了Ucrop库请添加混淆
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }