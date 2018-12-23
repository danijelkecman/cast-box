package com.cxromos.castbox.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cxromos.castbox.R;
import com.cxromos.castbox.data.model.Cast;
import com.cxromos.castbox.data.model.Track;
import com.cxromos.castbox.event.PauseEvent;
import com.cxromos.castbox.event.PlayNewEvent;
import com.cxromos.castbox.event.ResumeEvent;
import com.cxromos.castbox.event.StopEvent;
import com.cxromos.castbox.ui.track.TrackActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import timber.log.Timber;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MusicService.class.getSimpleName();

    public static final String EXTRA_TRACK = "com.cxromos.castbox.service.MusicService.EXTRA_TRACK";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";
    private static final String ACTION_RESUME = "action_resume";

    private static final int NOTIFICATION_ID = 400;
    private static final int REQUEST_CODE = 100;

    //MediaSession
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

    private MediaPlayer mediaPlayer;
    // url to the mTrack
    private Track mTrack;
    // used to pause/resume MediaPlayer
    private int resumePosition;
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private Cast mCast;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if(mManager == null) {
            initMediaSessions();
        }

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            handleAction(action);
        }

        if (intent != null) {
            mCast = intent.getParcelableExtra(TrackActivity.EXTRA_CAST);
            mTrack = intent.getParcelableExtra(EXTRA_TRACK);
            Log.d(TAG, "mCast: " + mCast);
            Log.d(TAG, "mTrack: " + mTrack);
        }
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        EventBus.getDefault().register(this);
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        if (mSession != null) {
            mSession.release();
        }

        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void initMediaSessions() {
        if (mediaPlayer == null) {
            initMediaPlayer();
        }
        mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.d(TAG, "onPlay");
                if(resumePosition != 0) {
                    Log.d(TAG, "onResume");
                    resumeMedia();
                } else {
                    Log.d(TAG, "playNewMedia");

                    playNewMedia();
                }
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d(TAG, "onPause");
                pauseMedia();
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.d(TAG, "onStop");
                stopMedia();
                NotificationManager notificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                //Intent intent = new Intent(getApplicationContext(), MusicService.class);
                // stopService(intent);
            }
        });
    }

    private void handleAction(String action) {
        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        } else if (action.equalsIgnoreCase(ACTION_RESUME)) {
            mController.getTransportControls().seekTo(resumePosition);
            mController.getTransportControls().play();
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        if (mTrack != null) {
            intent.putExtra(EXTRA_TRACK, mTrack);
        }
        PendingIntent pendingIntent = PendingIntent.getService(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(NotificationCompat.Action action) {
        if (mTrack != null) {
            android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle();

            Intent intent = new Intent(getApplicationContext(), MusicService.class);
            intent.setAction(ACTION_STOP);
            PendingIntent pendingIntent = PendingIntent.getService(this, REQUEST_CODE, intent, 0);
            android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(mTrack.title)
                    .setContentText(mTrack.author)
                    .setContentIntent(createContentIntent())
                    .setDeleteIntent(pendingIntent)
                    .setStyle(style);

            builder.addAction(action);
            style.setShowActionsInCompactView(0);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
        }
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(this, TrackActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(TrackActivity.EXTRA_CAST, mCast);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Subscribe
    public void onEvent(PlayNewEvent playNewEvent) {
        Log.d(TAG, "OnPlayNewEvent");
        mTrack = playNewEvent.getTrack();
        Log.d(TAG, "Position: " + resumePosition);
        handleAction(ACTION_PLAY);
    }

    @Subscribe
    public void onEvent(PauseEvent pauseEvent) {
        Log.d(TAG, "OnPauseEvent");
        if(mediaPlayer.isPlaying()) {
            // mediaPlayer.pause();
            handleAction(ACTION_PAUSE);
        }
    }

    @Subscribe
    public void onEvent(ResumeEvent resumeEvent) {
        if(!mediaPlayer.isPlaying()) {
            //resumeMedia();
            handleAction(ACTION_RESUME);
        }
    }

    @Subscribe
    public void onEvent(StopEvent stopEvent) {
        Log.d(TAG, "OnStopEvent");
        if (mediaPlayer.isPlaying()) {
            handleAction(ACTION_STOP);
        }
    }

    private void playNewMedia() {
        Log.d(TAG, "playNewMedia");
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mTrack.urls.get(0));
            resumePosition = 0;
        } catch (IOException e) {
            e.printStackTrace();
            // stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        Log.d(TAG, "playMedia");
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        Log.d(TAG, "stopMedia");
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        Log.d(TAG, "pauseMedia");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            Log.d(TAG, "pauseMedia resumePosition: " + resumePosition);
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            Log.d(TAG, "resumeMedia with resumePosition: " + resumePosition);
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        // stop the service
        // stopSelf();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Timber.d("MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Timber.d("MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Timber.d("MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    // binder given to clients
    private final IBinder iBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // Invoked indicating buffering status of
        // a media resource being streamed over the network.
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // Invoked to communicate some info.
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // Invoked indicating the completion of a seek operation.
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}