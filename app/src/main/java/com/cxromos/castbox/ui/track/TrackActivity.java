package com.cxromos.castbox.ui.track;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cxromos.castbox.R;
import com.cxromos.castbox.data.model.Cast;
import com.cxromos.castbox.data.model.Track;
import com.cxromos.castbox.service.MusicService;
import com.cxromos.castbox.ui.base.BaseActivity;
import com.cxromos.castbox.util.DialogFactory;
import com.cxromos.castbox.util.ItemClickSupport;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class TrackActivity extends BaseActivity implements TrackMvpView {
    private static final String TAG = TrackActivity.class.getSimpleName();

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.toolbar_collapsing) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.progress_indicator) ProgressBar mProgressBar;
    @Bind(R.id.recycler_tracks) RecyclerView mTrackRecycler;

    @Inject TracksAdapter mTracksAdapter;
    @Inject TrackPresenter mTrackPresenter;

    public static final String EXTRA_CAST = "com.cxromos.castbox.ui.activity.CastDetailsActivity.EXTRA_CAST";
    private Cast mCast;
    private List<Track> mTracks;

    private MusicService player;
    boolean serviceBound = false;

    public static Intent getStartIntent(Context context, Cast cast) {
        Intent intent = new Intent(context, TrackActivity.class);
        intent.putExtra(EXTRA_CAST, cast);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_track);
        ButterKnife.bind(this);

        mCast = getIntent().getParcelableExtra(EXTRA_CAST);
        if (mCast == null) {
            throw new IllegalArgumentException("CastDetailsActivity requires a Cast object!");
        }

        setupToolbar();
        setupRecyclerView();

        mTrackPresenter.attachView(this);
        mTrackPresenter.loadTracks(mCast.key);

        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTrackPresenter.detachView();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    /***** MVP View methods implementation *****/

    @Override
    public void showTracks(List<Track> tracks) {
        mProgressBar.setVisibility(View.GONE);
        for (Track track : tracks) {
            if(track.cover == null || track.cover.isEmpty()) {
                track.cover = mCast.coverMedium;
            }
        }
        mTracks = tracks;
        mTracksAdapter.setTracks(mTracks);
    }

    @Override
    public void showTracksEmpty() {
        mProgressBar.setVisibility(View.GONE);
        mTracksAdapter.setTracks(Collections.<Track>emptyList());
    }

    @Override
    public void showError() {
        mProgressBar.setVisibility(View.GONE);
        DialogFactory.createSimpleErrorDialog(TrackActivity.this).show();
    }

    /***** Setup View methods *****/

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            mCollapsingToolbar.setTitle(mCast.title);
        }
    }

    private void setupRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mTrackRecycler.setLayoutManager(layoutManager);
        mTrackRecycler.setAdapter(mTracksAdapter);
    }

    /********** Media Service *******/

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(TrackActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void startService() {
        if (!serviceBound) {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra(EXTRA_CAST, mCast);
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
