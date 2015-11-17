package info.device.deviceinfo.utils;

import android.app.Application;

/**
 * Created by namphan on 11/16/15.
 */
public class DeviceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppManager.create(this);
    }
}
