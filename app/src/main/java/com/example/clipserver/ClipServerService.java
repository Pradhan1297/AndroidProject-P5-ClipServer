package com.example.clipserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.clipservercommon.MusicPlayerInterface;

import java.util.Arrays;
import java.util.List;

public class ClipServerService extends Service {
    private MediaPlayer mediaPlayer;
    List<Integer> musicClipList;
    int currLength;
    private static final String CHANNEL_ID = "Music player" ;
    private static final int NOTIFICATION_ID = 1;
    private int mStartID;

    public ClipServerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicClipList = Arrays.asList(R.raw.august,R.raw.exil,R.raw.i_aint_worried,R.raw.polozhenie,R.raw.money);
    }


   private final MusicPlayerInterface.Stub musicBinder = new MusicPlayerInterface.Stub() {
       @Override
       public synchronized void play(int clipNumber) {
           CharSequence name = "Music player notification";
           String description = "The channel for music player notifications";
           int importance = NotificationManager.IMPORTANCE_DEFAULT;
           if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
               NotificationChannel channel;
               channel = new NotificationChannel(CHANNEL_ID, name, importance);
               channel.setDescription(description);
               // Register the channel with the system; you can't change the importance
               // or other notification behaviors after this
               NotificationManager notificationManager = getSystemService(NotificationManager.class);
               notificationManager.createNotificationChannel(channel);
           }

           Intent notificationIntent = new Intent();
           notificationIntent.putExtra("status","SaveState");
           notificationIntent.setComponent(new ComponentName("com.example.audioclient","com.example.audioclient.MainActivity"));


           final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_IMMUTABLE);

           Notification notification;


           notification =  new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                   .setSmallIcon(android.R.drawable.ic_media_play)
                   .setOngoing(true).setContentTitle("Music Playing")
                   .setContentText("Click to Access Music Player")
                   .setTicker("Music is playing!")
                   .setContentIntent(pendingIntent)
                   .addAction(R.drawable.ic_launcher, "Show service", pendingIntent)
                   .build();
           startForeground(NOTIFICATION_ID,notification);
           if(mediaPlayer!=null && mediaPlayer.isPlaying()){
               stopMusic();
           }
           mediaPlayer = MediaPlayer.create(getApplicationContext(), musicClipList.get(clipNumber-1));
           mediaPlayer.setLooping(false);
           mediaPlayer.setOnCompletionListener(mediaPlayer ->{
                   stopSelf();
                   stopMusic();});
           mediaPlayer.start();
       }

       @Override
       public synchronized void pause() {
           mediaPlayer.pause();
           currLength = mediaPlayer.getCurrentPosition();
       }

       @Override
       public synchronized void stop() {
           mediaPlayer.stop();
       }

       @Override
       public synchronized void resume() {
        mediaPlayer.seekTo(currLength);
        mediaPlayer.start();
       }

       @Override
       public synchronized void stopClipService() throws RemoteException {
           mediaPlayer.stop();
           mediaPlayer.release();
       }
   };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Don't automatically restart this Service if it is killed
        return START_NOT_STICKY;
    }

    private void stopMusic() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {

        Log.i("server","destroy");
        if (null != mediaPlayer) {

            mediaPlayer.stop();
            mediaPlayer.release();

        }
    }
}