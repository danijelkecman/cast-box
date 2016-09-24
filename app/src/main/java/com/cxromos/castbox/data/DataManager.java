package com.cxromos.castbox.data;

import com.cxromos.castbox.data.local.PreferencesHelper;
import com.cxromos.castbox.data.model.Casts;
import com.cxromos.castbox.data.model.Tracks;
import com.cxromos.castbox.data.remote.CastBoxService;

import javax.inject.Inject;

import rx.Observable;

public class DataManager {
    private final CastBoxService mCastBoxService;
    private final PreferencesHelper mPreferencesHelper;

    @Inject
    public DataManager(CastBoxService watchTowerService, PreferencesHelper preferencesHelper) {
        mCastBoxService = watchTowerService;
        mPreferencesHelper = preferencesHelper;
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Observable<Casts> getMedia(final String country, final int skip, final int top) {
        return mCastBoxService.getMedia(country, skip, top);
    }

    public Observable<Tracks> getTracks(final String key) {
        return mCastBoxService.getTracks(key);
    }
}
