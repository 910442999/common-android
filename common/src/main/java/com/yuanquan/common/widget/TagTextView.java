package com.yuanquan.common.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.yuanquan.common.R;

import java.util.List;

/**
 *   private fun setTextTagContent(holder: VH, string: String) {
 *         val dataList = arrayListOf<String>()
 *         dataList.add(LanguageUtils.optString("当前选择"))
 *         val dataListType = arrayListOf<Int>()
 *         dataListType.add(0)
 *         holder.binding.tvFileName.setContentAndTag(string, dataList, dataListType)
 *
 *     }
 * CSDN深海呐 https://blog.csdn.net/qq_40945489/article/details/109399596
 */
public class TagTextView extends AppCompatTextView {

    private Context mContext;
    private TextView mTabText;
    private StringBuffer mContentStringBuffer;

    //必须重写所有的构造器，否则可能会出现无法inflate布局的错误！
    public TagTextView(Context context) {
        super(context);
        mContext = context;
    }


    public TagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    /**
     * @param content
     * @param dataList 长度 需要与 typeList长度对应
     * @param typeList
     */
    public void setContentAndTag(String content, List<String> dataList, List<Integer> typeList) {
        mContentStringBuffer = new StringBuffer();
        for (String item : dataList) {//将内容添加到content，用drawable替代这些内容所占的位置
            mContentStringBuffer.append(item);
        }
        mContentStringBuffer.append(content);
        SpannableString spannableString = new SpannableString(mContentStringBuffer);
        for (int i = 0; i < dataList.size(); i++) {
            String item = dataList.get(i);
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_texttab, null);//R.layout.tag是每个标签的布局
            switch (typeList.get(i)) {
                case 1:
//                    view = LayoutInflater.from(mContext).inflate(R.layout.layout_texttab1, null);
                    break;
                case 2:
//                    view = LayoutInflater.from(mContext).inflate(R.layout.layout_texttab2, null);
                    break;
            }
            mTabText = view.findViewById(R.id.tabText);
            mTabText.setText(item);
            Bitmap bitmap = convertViewToBitmap(view);
            Drawable d = new BitmapDrawable(bitmap);
            d.setBounds(0, 0, mTabText.getWidth(), mTabText.getHeight());
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);//图片对齐底部
            int startIndex;
            int endIndex;
            startIndex = getLastLength(dataList, i);
            endIndex = startIndex + item.length();
            spannableString.setSpan(span, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setText(spannableString);
        setGravity(Gravity.CENTER_VERTICAL);
    }


    private static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }


    private int getLastLength(List<String> list, int maxLength) {
        int length = 0;
        for (int i = 0; i < maxLength; i++) {
            length += list.get(i).length();
        }
        return length;
    }
}