package com.example.virus;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrankService extends Service {

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private WindowManager manager;
    private View warning;

    private boolean running = true;
    private String lastApp = "";
    private long lastEventTime = 0;

    private final Runnable scanner = new Runnable() {
        @Override
        public void run() {

            if (!running) return;

            try {
                UsageStatsManager usage =
                        (UsageStatsManager)
                        getSystemService(USAGE_STATS_SERVICE);

                long now = System.currentTimeMillis();

                UsageEvents events = usage.queryEvents(
                        now - 10000,
                        now
                );

                UsageEvents.Event event =
                        new UsageEvents.Event();

                String currentApp = null;
                long newestTime = 0;

                while (events.hasNextEvent()) {

                    events.getNextEvent(event);

                    int type = event.getEventType();

                    if (type ==
                            UsageEvents.Event.MOVE_TO_FOREGROUND
                            || type ==
                            UsageEvents.Event.ACTIVITY_RESUMED) {

                        if (event.getTimeStamp() > newestTime) {
                            newestTime = event.getTimeStamp();
                            currentApp = event.getPackageName();
                        }
                    }
                }

                if (currentApp != null) {

                    if (!currentApp.equals(getPackageName())
                            && !currentApp.equals(lastApp)
                            && !currentApp.contains("launcher")
                            && !currentApp.contains("systemui")) {

                        lastApp = currentApp;

                        showWarning();
                    }
                }

            } catch (Exception ignored) {
            }

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        manager = (WindowManager)
                getSystemService(WINDOW_SERVICE);

        NotificationManager notifications =
                (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        notifications.createNotificationChannel(
                new NotificationChannel(
                        "virus_prank",
                        "Virus Prank Active",
                        NotificationManager.IMPORTANCE_LOW
                )
        );

        Notification notification =
                new Notification.Builder(this, "virus_prank")
                        .setContentTitle("Virus prank is active")
                        .setContentText(
                                "Tap the golden coin to stop"
                        )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_alert
                        )
                        .build();

        startForeground(7, notification);

        handler.post(scanner);
    }

    private void showWarning() {

        if (warning != null) return;

        LinearLayout box = new LinearLayout(this);

        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(30, 40, 30, 40);
        box.setBackgroundColor(0xEE150000);

        TextView title = new TextView(this);

        title.setText(
                "⚠ VIRUS DETECTED!\nAPP LOCKED!"
        );

        title.setTextColor(Color.RED);
        title.setTextSize(36);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        box.addView(title);

        TextView message = new TextView(this);

        message.setText(
                "\n🦠 FAKE VIRUS PRANK 🦠\n\n" +
                "Your apps and files are safe!\n"
        );

        message.setTextColor(Color.WHITE);
        message.setTextSize(18);
        message.setGravity(Gravity.CENTER);

        box.addView(message);

        Button coin = new Button(this);

        coin.setText("🪙 STOP PRANK");
        coin.setTextSize(25);
        coin.setTextColor(Color.BLACK);

        coin.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        0xFFFFD54F
                )
        );

        coin.setOnClickListener(v -> stopSelf());

        box.addView(coin);

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity = Gravity.TOP;

        warning = box;

        try {
            manager.addView(warning, params);
        } catch (Exception e) {
            warning = null;
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        running = false;

        handler.removeCallbacksAndMessages(null);

        if (warning != null) {
            try {
                manager.removeView(warning);
            } catch (Exception ignored) {
            }
            warning = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
