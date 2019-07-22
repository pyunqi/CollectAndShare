package com.yupa.stuffshare.service;

import android.content.Context;

import com.yupa.stuffshare.db.DBController;
import com.yupa.stuffshare.entity.Stuff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StuffLocalService {

    private  static DBController dbController;

    public static List<Stuff> getStuffs(Context context) {

        List<Stuff> stuffs = new ArrayList<>();
        dbController = new DBController(context);
        if (dbController.getAllStuff().isEmpty()) {

        } else {
            for (Stuff stuff : dbController.getAllStuff()) {
                stuffs.add(stuff);
            }
        }
        return stuffs;
    }

    public static List<Stuff> getStuffsByName(Context context, String name) {

        List<Stuff> stuffs = new ArrayList<>();
        dbController = new DBController(context);
        if (dbController.getAllStuff().isEmpty()) {

        } else {
            for (Stuff stuff : dbController.getAllStuffByName(name)) {
                stuffs.add(stuff);
            }
        }
        return stuffs;
    }

    public static void deleteStuff(Context context,int _id,String path){
        dbController = new DBController(context);
        dbController.deleteStuff(_id);
        DelFile dF = new DelFile(path);
        dF.start();
    }

    public static void updateStuff(Context context,Stuff stuff){
        dbController = new DBController(context);
        dbController.updateStuff(stuff);
    }

    static class DelFile extends Thread {

        private String fPath;
        DelFile(String path){
            fPath = path;
        }

        @Override
        public void run() {
            super.run();
            File delFile = new File(fPath);
            delFile.delete();
        }
    }

}
