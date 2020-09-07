package com.ml.location;

import android.app.Application;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.mlsdk.common.MLApplication;

public class MlLocationApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(this);
        MLApplication.getInstance().setApiKey(config.getString("client/api_key"));
    }
}
