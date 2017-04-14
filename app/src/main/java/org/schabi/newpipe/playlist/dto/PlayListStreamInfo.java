package org.schabi.newpipe.playlist.dto;

import org.schabi.newpipe.extractor.AbstractStreamInfo;

import java.io.Serializable;

/**
 * This class represent an entry retrive by the join between playlist_entries and playlist_link_entries tables
 */
public class PlayListStreamInfo extends AbstractStreamInfo implements Serializable {

    public int playlistId;
    public int position;
    public int duration;

}
