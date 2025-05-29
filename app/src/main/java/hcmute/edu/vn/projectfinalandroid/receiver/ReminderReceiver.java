package hcmute.edu.vn.projectfinalandroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hcmute.edu.vn.projectfinalandroid.channel.Notification;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("reminder_id", -1);

        String message;
        switch (reminderId) {
            case 0:
                message = "🌞 Good morning! Let's learn 5 new words!";
                break;
            case 1:
                message = "🍽 Take a lunch break and review some vocabulary!";
                break;
            case 2:
                message = "🌙 It's evening, time to review today's words!";
                break;
            default:
                message = "🧠 It's time to study vocabulary!";
        }

        Notification.showNotification(context, "Learn with Lada", message);
    }
}


