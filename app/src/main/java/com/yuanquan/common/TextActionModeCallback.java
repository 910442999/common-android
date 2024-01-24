package com.yuanquan.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * @hide
 */
public class TextActionModeCallback implements ActionMode.Callback, TextWatcher{
    private View mCustomView;

    private InputMethodManager mInput;
    private Resources mResources;
    private boolean mMatchesFound;
    private int mNumberOfMatches;
    private int mActiveMatchIndex;
    private ActionMode mActionMode;

    public TextActionModeCallback(Context context) {
        mCustomView = LayoutInflater.from(context).inflate(
                R.layout.item_msg_text, null);

        mInput = context.getSystemService(InputMethodManager.class);
        mResources = context.getResources();
    }

    public void finish() {
        mActionMode.finish();
    }

    /**
     * Place text in the text field so it can be searched for.  Need to press
     * the find next or find previous button to find all of the matches.
     */
    public void setText(String text) {
//        mEditText.setText(text);
//        Spannable span = (Spannable) mEditText.getText();
//        int length = span.length();
//        // Ideally, we would like to set the selection to the whole field,
//        // but this brings up the Text selection CAB, which dismisses this
//        // one.
//        Selection.setSelection(span, length, length);
//        // Necessary each time we set the text, so that this will watch
//        // changes to it.
//        span.setSpan(this, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        mMatchesFound = false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.setCustomView(mCustomView);
//        mode.getMenuInflater().inflate(com.android.internal.R.menu.webview_find,
//                menu);
        mActionMode = mode;
//        Editable edit = mEditText.getText();
//        Selection.setSelection(edit, edit.length());
        mMatchesFound = false;
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        return true;
    }

    // TextWatcher implementation

    @Override
    public void beforeTextChanged(CharSequence s,
                                  int start,
                                  int count,
                                  int after) {
        // Does nothing.  Needed to implement TextWatcher.
    }

    @Override
    public void onTextChanged(CharSequence s,
                              int start,
                              int before,
                              int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        // Does nothing.  Needed to implement TextWatcher.
    }

    private Rect mGlobalVisibleRect = new Rect();
    private Point mGlobalVisibleOffset = new Point();

    public int getActionModeGlobalBottom() {
        if (mActionMode == null) {
            return 0;
        }
        View view = (View) mCustomView.getParent();
        if (view == null) {
            view = mCustomView;
        }
        view.getGlobalVisibleRect(mGlobalVisibleRect, mGlobalVisibleOffset);
        return mGlobalVisibleRect.bottom;
    }

    public static class NoAction implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
