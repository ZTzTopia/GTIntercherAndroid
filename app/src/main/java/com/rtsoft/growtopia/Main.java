package com.rtsoft.growtopia;

import android.os.Bundle;
import android.os.Environment;

import com.gt.launcher.BuildConfig;

import java.io.File;

public class Main extends SharedActivity {
    @Override
    public void onCreate(Bundle bundle) {
        BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArv12FD/xxuAJ3/B8Jgx78985UN/FitcQD5C21eIS5D+98yr7dy9sw8R2fSTFZKExBZVAfatgDH7s6fb9vfHi43szfpdXs3ZL2hsa7DeCWRyVSTD6o/i14vgwInv1S/dgLAwQth3PDXWF+zYXOlL+umOt9K9eqQo5CZhkwl9JAmMHlazvbhSGAldV5QsdY3pK5wmg/w2873abgYsGdI3B9wL75kgZW9tV2O6efiIbXlevktGOMup3Ql2H4Rcpa3ZeDtGl+YTQbEUQTYiYBDtFGCyqksXeM6+kCnaF97Ss5wA0w5ID9WJLkziXI4iGBMRd0a7s+vVniwpx771oGcJxewIDAQAB";

        dllname = "growtopia";
        securityEnabled = false;
        IAPEnabled = true;
        HookedEnabled = false;
        PackageName = BuildConfig.APPLICATION_ID;
        if (!new File(Environment.getExternalStorageDirectory().toString() + File.separatorChar + "windows" + File.separatorChar + "BstSharedFolder").exists()) {
            System.loadLibrary(dllname);
            super.onCreate(bundle);
        }
    }
}
