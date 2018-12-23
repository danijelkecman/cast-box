package com.cxromos.castbox.ui.track;

import com.cxromos.castbox.data.model.Track;
import com.cxromos.castbox.ui.base.MvpView;

import java.util.List;

public interface TrackMvpView extends MvpView {
    void showTracks(List<Track> tracks);
    void showTracksEmpty();
    void showError();
}
