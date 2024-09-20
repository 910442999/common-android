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