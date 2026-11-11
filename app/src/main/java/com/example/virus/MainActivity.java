package com.example.virus;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {

    private boolean hasUsageAccess() {
        AppOpsManager ops =
                (AppOpsManager) getSystemService(APP_OPS_SERVICE);

        return ops.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                getPackageName()
        ) == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(
                    this,
                    "Allow Virus to display over other apps.",
                    Toast.LENGTH_LONG
            ).show();

            Intent settings = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );

            startActivity(settings);

        } else if (!hasUsageAccess()) {
            Toast.makeText(
                    this,
                    "Allow Usage Access, then open Virus again.",
                    Toast.LENGTH_LONG
            ).show();

            startActivity(
                    new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            );

        } else {
            startService(new Intent(this, PrankService.class));

            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(home);
        }

        finish();
    }
}
