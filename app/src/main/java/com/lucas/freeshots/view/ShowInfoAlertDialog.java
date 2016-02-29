package com.lucas.freeshots.view;


import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.lucas.freeshots.R;


/**
 * 显示信息Dialog，包括标题，内容和一些操作按钮
 * 操作按钮左边最多1个，右边最多3个，可以没有。
 */
public class ShowInfoAlertDialog {

    public interface OnActionClickListener {
        void onClick(String actionName);
    }

    private String title;
    private String content;
    private String[] leftActionNames;
    private String[] rightActionNames;
    private Context context;
    private OnActionClickListener listener;

    public ShowInfoAlertDialog(Context context, String title, String content,
                               String[] leftActionNames, String[] rightActionNames,
                               OnActionClickListener listener) {
        this.title = title;
        this.content = content;
        this.leftActionNames = leftActionNames;
        this.rightActionNames = rightActionNames;
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        int leftLen = leftActionNames != null ? leftActionNames.length : 0;
        int rightLen = rightActionNames != null ? rightActionNames.length : 0;

        if(leftLen > 1 || rightLen > 3) {
            return;
        }

        View v = View.inflate(context, R.layout.alert_dialog_show_info, null);

        TextView titleTv = (TextView) v.findViewById(R.id.title);
        TextView contentTv = (TextView) v.findViewById(R.id.content);

        TextView actionLeft0Tv = (TextView) v.findViewById(R.id.actionLeft0);

        TextView actionRight0Tv = (TextView) v.findViewById(R.id.actionRight0);
        TextView actionRight1Tv = (TextView) v.findViewById(R.id.actionRight1);
        TextView actionRight2Tv = (TextView) v.findViewById(R.id.actionRight2);

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(v).show();

        View.OnClickListener onClickListener = (view) -> {
            if (listener != null) {
                listener.onClick(((TextView) view).getText().toString());
            }
            dialog.dismiss();
        };

        titleTv.setText(title);
        contentTv.setText(content);

        if(leftLen == 0) {
            actionLeft0Tv.setVisibility(View.INVISIBLE);
        } else {
            actionLeft0Tv.setText(leftActionNames[0]);
            actionLeft0Tv.setOnClickListener(onClickListener);
        }

        switch (rightLen) {
            case 3:
                actionRight2Tv.setText(rightActionNames[2]);
                actionRight2Tv.setOnClickListener(onClickListener);
            case 2:
                actionRight1Tv.setText(rightActionNames[1]);
                actionRight1Tv.setOnClickListener(onClickListener);
            case 1:
                actionRight0Tv.setText(rightActionNames[0]);
                actionRight0Tv.setOnClickListener(onClickListener);
        }

        switch (rightLen) {
            case 0:
                actionRight0Tv.setVisibility(View.INVISIBLE);
            case 1:
                actionRight1Tv.setVisibility(View.INVISIBLE);
            case 2:
                actionRight2Tv.setVisibility(View.INVISIBLE);
        }
    }
}
