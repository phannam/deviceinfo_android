package info.device.deviceinfo.utils;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by namphan on 11/16/15.
 */
public class AppManager {
    private static AppManager mInstance;
    private Context mContext;

    private ArrayList<Item> deviceInfoList;
    private ArrayList<Item> displayInfoList;

    public List<Item> getDeviceInfoList() {
        return deviceInfoList;
    }

    public List<Item> getDisplayInfoList() {
        return displayInfoList;
    }

    public String getAllInfos() {
        StringBuilder builder = new StringBuilder();
        builder.append("Device\n");
        for (Item item : deviceInfoList) {
            builder.append("\t" + item.getName() + ": " + item.getValue() + "\n");
        }
        builder.append("\n");
        builder.append("Display\n");
        for (Item item : displayInfoList) {
            builder.append("\t" + item.getName() + ": " + item.getValue() + "\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    private AppManager() {
    }

    private AppManager(Context context) {
        mContext = context;
    }

    public static synchronized void create(Context context) {
        if (mInstance == null) {
            mInstance = new AppManager(context);
        }
        mInstance.initializeInstance(context);
    }

    public static AppManager getInstance() {
        return mInstance;
    }

    private void initializeInstance(Context context) {
        initializeGeneralDeviceInfo();
        initializeDisplayInfo();
    }

    private void initializeGeneralDeviceInfo() {
        Item item;
        deviceInfoList = new ArrayList<>();
        item = new Item("Product", Build.PRODUCT);
        deviceInfoList.add(item);
        item = new Item("Device", Build.DEVICE);
        deviceInfoList.add(item);
        item = new Item("Board", Build.BOARD);
        deviceInfoList.add(item);
        item = new Item("Manufacturer", Build.MANUFACTURER);
        deviceInfoList.add(item);
        item = new Item("Brand", Build.BRAND);
        deviceInfoList.add(item);
        item = new Item("Model", Build.MODEL);
        deviceInfoList.add(item);
    }

    private void initializeDisplayInfo() {
        Item item;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        displayInfoList = new ArrayList<>();
        item = new Item("Display", Build.DISPLAY);
        displayInfoList.add(item);
        item = new Item("Width Pixels", "" + metrics.widthPixels);
        displayInfoList.add(item);
        item = new Item("Height Pixels", "" + metrics.heightPixels);
        displayInfoList.add(item);
        item = new Item("Density", "" + metrics.density);
        displayInfoList.add(item);
        item = new Item("DensityDpi", "" + metrics.densityDpi);
        displayInfoList.add(item);
        item = new Item("Scaled Density", "" + metrics.scaledDensity);
        displayInfoList.add(item);
        item = new Item("x dpi", "" + metrics.xdpi);
        displayInfoList.add(item);
        item = new Item("y dpi", "" + metrics.ydpi);
        displayInfoList.add(item);
        item = new Item("Default Density", "" + DisplayMetrics.DENSITY_DEFAULT);
        displayInfoList.add(item);
        item = new Item("Low Density", "" + DisplayMetrics.DENSITY_LOW);
        displayInfoList.add(item);
        item = new Item("Medium Density", "" + DisplayMetrics.DENSITY_MEDIUM);
        displayInfoList.add(item);
        item = new Item("High Density", "" + DisplayMetrics.DENSITY_HIGH);
        displayInfoList.add(item);
    }
}
