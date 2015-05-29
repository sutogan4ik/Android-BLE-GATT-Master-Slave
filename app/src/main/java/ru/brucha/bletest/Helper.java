package ru.brucha.bletest;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Prog on 27.05.2015.
 */
public class Helper {
    public static boolean checkBLE(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static String getServiceName(String uuid){
        switch (uuid){
            case "00001800-0000-1000-8000-00805f9b34fb":{
                return "Generic Access";
            }
            case "00001801-0000-1000-8000-00805f9b34fb":{
                return "Generic Attribute";
            }
            case "00001111-0000-1000-8000-00805f9b34fb":{
                return "Notification Service";
            }

            default:
                return "Unknown Service";
        }
    }

}
