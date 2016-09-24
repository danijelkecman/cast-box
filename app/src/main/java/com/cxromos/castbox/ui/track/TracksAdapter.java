package com.cxromos.castbox.ui.track;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cxromos.castbox.R;
import com.cxromos.castbox.data.model.Track;
import com.cxromos.castbox.event.PauseEvent;
import com.cxromos.castbox.event.ResumeEvent;
import com.cxromos.castbox.event.PlayNewEvent;
import com.cxromos.castbox.event.StopEvent;
import com.cxromos.castbox.util.LocalUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.TrackHolder> {
    static final int STATE_PLAYABLE = 1;
    static final int STATE_PAUSED = 2;
    static final int STATE_PLAYING = 3;

    private List<Track> mTracks;
    private EventBus mEventBus;

    private static ColorStateList sColorStatePlaying;
    private static ColorStateList sColorStateNotPlaying;

    @Inject
    public TracksAdapter() {
        this.mTracks = new ArrayList<>();
        this.mEventBus = EventBus.getDefault();
    }

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);

        return new TrackHolder(view);
    }

    @Override
    public void onBindViewHolder(final TrackHolder holder, final int p) {
        final Context context = holder.itemView.getContext();

        final int position = holder.getAdapterPosition();
        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(context);
        }

        final Track track = mTracks.get(position);
        holder.trackTitleText.setText(track.title);
        holder.trackDescriptionText.setText(LocalUtils.stripHtml(track.description));
        holder.trackReleaseDateText.setText(track.releaseDate);
        Glide.with(context)
                .load(track.cover)
                .into(holder.trackImage);

        updatePlayIconState(context, holder, track);

        holder.playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("State: " + track.state);
                if (track.state == STATE_PLAYABLE) {
                    if(isAnyTrackPlaying(position)) {
                        notifyDataSetChanged();
                    }
                    track.state = STATE_PLAYING;
                    mEventBus.post(new PlayNewEvent(track));
                    updatePlayIconState(context, holder, track);
                } else if (track.state == STATE_PAUSED) {
                    track.state = STATE_PLAYING;
                    mEventBus.post(new ResumeEvent());
                    updatePlayIconState(context, holder, track);
                } else if(track.state == STATE_PLAYING) {
                    track.state = STATE_PAUSED;
                    mEventBus.post(new PauseEvent());
                    updatePlayIconState(context, holder, track);
                }
                mTracks.set(position, track);
            }
        });
    }

    private boolean isAnyTrackPlaying(int currentPosition) {
        int position = 0;
        boolean currentlyPlaying = false;
        for (Track track : mTracks) {
            if (track.state == STATE_PLAYING || track.state == STATE_PAUSED || position != currentPosition) {
                track.state = STATE_PLAYABLE;
                mTracks.set(position, track);
                position++;
                currentlyPlaying = true;
            }
        }
        return currentlyPlaying;
    }

    private void updatePlayIconState(final Context context, TrackHolder holder, final Track track) {
        switch (track.state) {
            case STATE_PLAYABLE:
                Drawable pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_36dp);
                DrawableCompat.setTintList(pauseDrawable, sColorStateNotPlaying);
                holder.playImage.setImageDrawable(pauseDrawable);
                holder.playImage.setVisibility(View.VISIBLE);
                break;
            case STATE_PLAYING:
                AnimationDrawable animation = (AnimationDrawable)
                        ContextCompat.getDrawable(context, R.drawable.ic_equalizer_white_36dp);
                DrawableCompat.setTintList(animation, sColorStatePlaying);
                holder.playImage.setImageDrawable(animation);
                holder.playImage.setVisibility(View.VISIBLE);
                animation.start();
                break;
            case STATE_PAUSED:
                Drawable playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_equalizer1_white_36dp);
                DrawableCompat.setTintList(playDrawable, sColorStatePlaying);
                holder.playImage.setImageDrawable(playDrawable);
                holder.playImage.setVisibility(View.VISIBLE);
                break;
            default:
                holder.playImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public void setTracks(List<Track> tracks) {
        mTracks = tracks;
        notifyDataSetChanged();
    }

    static private void initializeColorStateLists(Context ctx) {
        sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(R.color.media_item_icon_not_playing));
        sColorStatePlaying = ColorStateList.valueOf(ctx.getResources().getColor(R.color.media_item_icon_playing));
    }

    class TrackHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.track_title) TextView trackTitleText;
        @Bind(R.id.track_description) TextView trackDescriptionText;
        @Bind(R.id.track_release_date) TextView trackReleaseDateText;
        @Bind(R.id.track_image) ImageView trackImage;
        @Bind(R.id.play_eq) ImageView playImage;

        public TrackHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
