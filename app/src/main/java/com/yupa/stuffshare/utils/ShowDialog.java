package com.yupa.stuffshare.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ShowDialog {
    public static void show(Context context, String message){
        Toast toast = Toast.makeText(context,message,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
}
