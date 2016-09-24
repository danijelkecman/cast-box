package com.cxromos.castbox.event;

import com.cxromos.castbox.data.model.Track;

public class PlayNewEvent {
    private Track track;

    public PlayNewEvent(Track track) {
        this.track = track;
    }

    public Track getTrack() {
        return track;
    }
}
