package com.yuanquan.common.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * timeCompare 判断2个时间大小
 */
public class TimeUtils {
    /**
     * 设置每个阶段时间
     */
    private static final int seconds_of_1minute = 60;

    private static final int seconds_of_30minutes = 30 * 60;

    private static final int seconds_of_1hour = 60 * 60;

    private static final int seconds_of_1day = 24 * 60 * 60;

    private static final int seconds_of_15days = seconds_of_1day * 15;

    private static final int seconds_of_30days = seconds_of_1day * 30;

    private static final int seconds_of_6months = seconds_of_30days * 6;

    private static final int seconds_of_1year = seconds_of_30days * 12;
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyy_MM_dd = "yyyy-MM-dd";
    public static final String MM_dd = "MM-dd";
    public static final String HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String MM_dd_HH_mm = "MM-dd HH:mm";


    //时间转换 2018-12-27T16:00:00.000Z” 转换成“yyyy-MM-dd”
    public static String getPreCallbackTime(String timeData) {
        if (TextUtils.isEmpty(timeData)) {
            return "";
        }
        if (!timeData.contains("T")) {
            return timeData;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
        try {
            Date time = format.parse(timeData);
            String date = time.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
            Date datetime = (Date) sdf.parse(date);
            /*datetime = new java.sql.Date(datetime.getTime());*/
            timeData = new SimpleDateFormat("MM-dd HH:mm").format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeData;
    }

    //时间转换 2018-12-27T16:00:00.000Z” 转换成“yyyy-MM-dd”
    public static String getPreCallbackTimeNew(String preCallbackTimeStart) {
        if (!preCallbackTimeStart.contains("T")) {
            return preCallbackTimeStart;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
        try {
            Date time = format.parse(preCallbackTimeStart);
            String date = time.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US);
            Date datetime = (Date) sdf.parse(date);
            /*datetime = new java.sql.Date(datetime.getTime());*/
            preCallbackTimeStart = new SimpleDateFormat("yyyy-MM-dd").format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return preCallbackTimeStart;
    }

    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }

    /**
     * yyyy-MM-dd HH:mm:ss转换为yyyy-MM-dd
     *
     * @throws ParseException
     * @author 刘鹏
     */
    public static String transferFormat(String inTime, String old_pattern, String new_pattern) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat(old_pattern);
            Date date = sdf1.parse(inTime);
            SimpleDateFormat sdf2 = new SimpleDateFormat(new_pattern);
            String outTime = sdf2.format(date);
            return outTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * yyyy-MM-dd HH:mm:ss转换为yyyy-MM-dd
     *
     * @throws ParseException
     * @author 刘鹏
     */
    public static String transferFormat(String inTime, String pattern) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
            Date date = sdf1.parse(inTime);
            SimpleDateFormat sdf2 = new SimpleDateFormat(pattern);
            String outTime = sdf2.format(date);
            return outTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long stringToMillis(final String time) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 判断2个时间大小
     * yyyy-MM-dd HH:mm 格式（自己可以修改成想要的时间格式）
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static int timeCompare(String startTime, String endTime, String pattern) {
        int i = 0;
        //注意：传过来的时间格式必须要和这里填入的时间格式相同
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            Date date1 = dateFormat.parse(startTime);//开始时间
            Date date2 = dateFormat.parse(endTime);//结束时间
            // 1 结束时间小于开始时间 2 开始时间与结束时间相同 3 结束时间大于开始时间
            if (date2.getTime() < date1.getTime()) {
                //结束时间小于开始时间
                i = 1;
            } else if (date2.getTime() == date1.getTime()) {
                //开始时间与结束时间相同
                i = 2;
            } else if (date2.getTime() > date1.getTime()) {
                //结束时间大于开始时间
                i = 3;
            }
        } catch (Exception e) {

        }
        return i;
    }

    /**
     * 格式化时间
     *
     * @param mTime
     * @return
     */
    public static String getTimeRange(String mTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /**获取当前时间*/
        Date curDate = new Date(System.currentTimeMillis());
        String dataStrNew = sdf.format(curDate);
        Date startTime = null;
        try {
            /**将时间转化成Date*/
            curDate = sdf.parse(dataStrNew);
            startTime = sdf.parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /**除以1000是为了转换成秒*/
        long between = (curDate.getTime() - startTime.getTime()) / 1000;
        int elapsedTime = (int) (between);
        if (elapsedTime < seconds_of_1minute) {

            return "刚刚";
        }
        if (elapsedTime < seconds_of_30minutes) {

            return elapsedTime / seconds_of_1minute + "分钟前";
        }
        if (elapsedTime < seconds_of_1hour) {

            return "半小时前";
        }
        if (elapsedTime < seconds_of_1day) {

            return elapsedTime / seconds_of_1hour + "小时前";
        }
        if (elapsedTime < seconds_of_15days) {

            return elapsedTime / seconds_of_1day + "天前";
        }
        if (elapsedTime < seconds_of_30days) {

            return "半个月前";
        }
        if (elapsedTime < seconds_of_6months) {

            return elapsedTime / seconds_of_30days + "月前";
        }
        if (elapsedTime < seconds_of_1year) {

            return "半年前";
        }
        if (elapsedTime >= seconds_of_1year) {

            return elapsedTime / seconds_of_1year + "年前";
        }
        return mTime;
    }

    public static String stringForTimeMS(long timeS) {
        long seconds = timeS % 60;
        long minutes = timeS / 60 % 60;
        return minutes + ":" + seconds;
    }

    /**
     * 拼团日期差异
     *
     * @param endTime
     */
    public static String groupPurchaseDateDiff(Context context, String endTime) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数long diff;try {
        // 获得两个时间的毫秒时间差异
        try {
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String startTime = sd.format(curDate);
            long diff = sd.parse(endTime).getTime()
                    - sd.parse(startTime).getTime();
            long day = diff / nd;// 计算差多少天
            long hour = diff % nd / nh;// 计算差多少小时
            long min = diff % nd % nh / nm;// 计算差多少分钟
            long sec = diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
            String s = (hour > 0 ? (hour < 10 ? "0" + hour : hour) : "00") + ":" + (min > 0 ? (min < 10 ? "0" + min : min) : "00") + ":" + (sec > 0 ? (sec < 10 ? "0" + sec : sec) : "00");

            if (day > 0) {
//                return String.format(context.getResources().getString(R.string.group_purchase_end), day + "", s);
            } else {
//                return String.format(context.getResources().getString(R.string.group_purchase_end_hour), s);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public static String toTimeStr(int secTotal) {

        String result = null;
        secTotal = secTotal / 1000;
        int hour = secTotal / 3600;

        result = String.valueOf(hour);

        return result;
    }


    //限时优惠 。时间差
    public static String groupPurchaseDateDiff1(Context context, String endTime) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(yyyy_MM_dd_HH_mm_ss);
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数
        long ns = 1000;// 一秒钟的毫秒数
        // 获得两个时间的毫秒时间差异
        try {
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String startTime = sd.format(curDate);
            long diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();
            long day = diff / nd;// 计算差多少天
            long hour = diff % nd / nh;// 计算差多少小时
            long min = diff % nd % nh / nm;// 计算差多少分钟
            long sec = diff % nd % nh % nm / ns;// 计算差多少秒//输出结果
            //            long times = (day * 24) + hour;
            //            return times > 0 ? times : 0;

            String s = (hour > 0 ? (hour < 10 ? "0" + hour : hour) : "00") + ":" + (min > 0 ? (min < 10 ? "0" + min : min) : "00") + ":" + (sec > 0 ? (sec < 10 ? "0" + sec : sec) : "00");

            if (day > 0) {

//                return String.format(context.getResources().getString(R.string.hours_from_start), day + "", s);
            } else {
//                return String.format(context.getResources().getString(R.string.hours_from_start_hour), s);
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取系统时间戳
     *
     * @return
     */
    public static long getCurTimeLong() {
        long time = System.currentTimeMillis();
        return time;
    }

    /**
     * 获取当前时间
     *
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurDate(String pattern) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date());
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param milSecond
     * @param pattern   yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 将字符串转为时间戳
     *
     * @param dateString
     * @param pattern    yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * date转string
     *
     * @param date
     * @param format
     * @return
     */
    public static String getDateTime(Date date, String format) {
        if (date == null)
            return "";
        String currentTime = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            currentTime = sdf.format(date);
            return currentTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTime;
    }

    /**
     * @param time   时间戳
     * @param format
     * @return
     */
    public static String getDateTime(String time, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            long lt = new Long(time);
            Date date = new Date(lt);
            String res = simpleDateFormat.format(date);
            return res;
        } catch (Exception e) {
            return "";
        }

    }

    public static String getTimedata(String user_time) {
        String re_time = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d;
        try {
            d = sdf.parse(user_time);
            long l = d.getTime();
            String str = String.valueOf(l);
            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            // TODO Auto-generated catch block e.printStackTrace();
        }
        return re_time;
    }

    /**
     * 将某种格式的时间转为时间戳
     *
     * @param timeString
     * @return
     */
    public static String getTime(String timeString, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d;
        try {
            d = sdf.parse(timeString);
            return d.getTime() + "";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 时间戳转date
     *
     * @param strDate
     * @return
     */
    public static Date parse(String strDate) {
        Date date = new Date();
        try {
            Long ll = new Long(strDate);
            date = new Date(ll);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return date;
    }


    /**
     * @param birthday 时间戳
     * @return
     */
    public static String getAge(String birthday, String format) {
        try {
            long lt = new Long(getTime(birthday, format));
            Date date = new Date(lt);

            Calendar cal = Calendar.getInstance();
            if (cal.before(birthday)) { //出生日期晚于当前时间，无法计算
                return "0";
            }
            int yearNow = cal.get(Calendar.YEAR);  //当前年份
            int monthNow = cal.get(Calendar.MONTH);  //当前月份
            int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
            cal.setTime(date);
            int yearBirth = cal.get(Calendar.YEAR);
            int monthBirth = cal.get(Calendar.MONTH);
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            int age = yearNow - yearBirth;   //计算整岁数
            if (monthNow <= monthBirth) {
                if (monthNow == monthBirth) {
                    if (dayOfMonthNow < dayOfMonthBirth)
                        age--;//当前日期在生日之前，年龄减一
                } else {
                    age--;//当前月份在生日之前，年龄减一

                }
            }
            return age + "";
        } catch (Exception e) {
            return "";
        }
    }

//    /***
//     * @param timeps 时间戳
//     * @param time   时间戳
//     */
//    public static boolean isSameDay(String timeps, String time) {
//        try {
//            Long ll = new Long(timeps);
//            Date datefirst = new Date(ll);
//            Long ll_second = new Long(time);
//            Date dateSecond = new Date(ll_second);
//            Calendar cal1 = Calendar.getInstance();
//            cal1.setTime(datefirst);
//            Calendar cal2 = Calendar.getInstance();
//            cal2.setTime(dateSecond);
//            return isSameDay(cal1, cal2);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//        return false;
//    }
//
//    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
//        if (cal1 != null && cal2 != null) {
//            return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
//        } else {
//            throw new IllegalArgumentException("The date must not be null");
//        }
//    }


    public static String getTime(String time) {
        String result = "";
        if (!TextUtils.isEmpty(time)) {
            String startTime = time.split("T")[0];
            if (!TextUtils.isEmpty(startTime)) {
                String[] timeArr = startTime.split("-");
                result = timeArr[0] + "." + timeArr[1] + "." + timeArr[2];
            }
        }
        return result;
    }


    public static String toTime(String s, long start) {
        long millisecond = start % 1000;
        millisecond /= 100;
        start /= 1000;
        long minute = start / 60;
        long second = start % 60;
        minute %= 60;

        Log.e(s + "==============", String.format("%02d:%02d:%01d", minute, second, millisecond));
        return String.format("%02d:%02d:%01d", minute, second, millisecond);
    }

    public static String StringToDate(String time) throws ParseException {

        if (TextUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(time);
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd");
        String s = format1.format(date);
        return s;

    }

    // 将时间戳转为字符串
    public static String getStrTime(String cc_time) {
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));
        return re_StrTime;
    }
}
