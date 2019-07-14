package com.yupa.stuffshare.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

public class ShowMessage {
    public static void showCenter(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static boolean confirmDialog(Context context){
        boolean flag = false;
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(context);
        confirmBuilder.setMessage("Write your message here.");
        confirmBuilder.setCancelable(true);

        confirmBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        confirmBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog confirm = confirmBuilder.create();
        confirm.show();
        return flag;
    }
}

