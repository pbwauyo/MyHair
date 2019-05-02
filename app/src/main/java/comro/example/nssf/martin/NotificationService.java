package comro.example.nssf.martin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import comro.example.nssf.martin.customer.CustomerMainPage;
import comro.example.nssf.martin.stylist.StylistMainPage;

public class NotificationService extends FirebaseMessagingService {
    private NotificationCompat.Builder builder;
    private String notificationType;
    private final String HAIR_REQUESTS_CHANNEL_ID = "1";
    private final String REQUESTS_REPLIES_CHANNEL_ID = "2";
    private int notificationId;
    private NotificationManagerCompat notificationManager;
    private Uri soundUri;
    private AudioAttributes attributes;
    private PendingIntent stylistChatPendingIntent, customerPendingIntent;
    private Intent stylistChatIntent, customerChatIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.consequence);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
        }

        //intent for viewing stylist chats
        stylistChatIntent = new Intent(this, StylistMainPage.class);
        stylistChatIntent.putExtra("displayFragment","view_chats");
        stylistChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        stylistChatPendingIntent = PendingIntent.getActivity(this, 0, stylistChatIntent, 0);

        //intent for customer chats
        customerChatIntent = new Intent(this, CustomerMainPage.class);
        customerChatIntent.putExtra("displayFragment", "view_chats");
        customerChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        customerPendingIntent = PendingIntent.getActivity(this, 0, customerChatIntent, 0);;

        createHairRequestsChannel();
        createRequestsRepliesChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        notificationId = new Random().nextInt();
        notificationType = remoteMessage.getData().get("type");

        switch (notificationType){
            case "hairRequest":
                builder = new NotificationCompat.Builder(this, HAIR_REQUESTS_CHANNEL_ID)
                        .setSmallIcon(R.drawable.salonicon)
                        .setSound(soundUri)
                        .setContentTitle("New hair request")
                        .setContentText("You have received a new hair request")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("You have received a new hair request"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(stylistChatPendingIntent)
                        .setAutoCancel(true);

                notificationManager.notify(notificationId, builder.build());
                break;

            case "accepted":
                builder = new NotificationCompat.Builder(this, REQUESTS_REPLIES_CHANNEL_ID)
                        .setSmallIcon(R.drawable.salonicon)
                        .setSound(soundUri)
                        .setContentTitle("Request Reply")
                        .setContentText("Your hair request hair request has successfully been approved")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Your hair request hair request has successfully been approved. Tap to view more"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(customerPendingIntent)
                        .setAutoCancel(true);

                notificationManager.notify(notificationId, builder.build());
                break;

            case "declined":
                builder = new NotificationCompat.Builder(this, REQUESTS_REPLIES_CHANNEL_ID)
                        .setSmallIcon(R.drawable.salonicon)
                        .setSound(soundUri)
                        .setContentTitle("Request Reply")
                        .setContentText("Unfortunately, your hair request has been declined")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Unfortunately, your hair request has been declined. Tap to view more"))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(customerPendingIntent)
                        .setAutoCancel(true);

                notificationManager.notify(notificationId, builder.build());
                break;
        }
    }

    private void createHairRequestsChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.hair_requests_channel);
            String description = getString(R.string.hair_requests_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(HAIR_REQUESTS_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(soundUri, attributes);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createRequestsRepliesChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.requests_replies);
            String description = getString(R.string.requests_replies_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(REQUESTS_REPLIES_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(soundUri, attributes);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
