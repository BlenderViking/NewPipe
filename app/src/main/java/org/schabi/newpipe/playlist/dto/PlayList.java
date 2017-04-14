package org.schabi.newpipe.playlist.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayList implements Serializable {

    private final int _id;
    private final String name;
    private final List<PlayListStreamInfo> entries;

    public PlayList(int id, final String name, final ArrayList<PlayListStreamInfo> entries) {
        _id = id;
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public int get_id() {
        return _id;
    }

    public List<PlayListStreamInfo> getEntries() {
        return entries;
    }

    public void swapEntries(final int entriesA, final int entriesB) {
        Collections.swap(entries, entriesA, entriesB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayList playList = (PlayList) o;

        if (_id != playList._id) return false;
        if (name != null ? !name.equals(playList.name) : playList.name != null) return false;
        return entries != null ? entries.equals(playList.entries) : playList.entries == null;

    }

    @Override
    public int hashCode() {
        int result = _id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (entries != null ? entries.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayList{");
        sb.append("_id=").append(_id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", entries=").append(entries);
        sb.append('}');
        return sb.toString();
    }
}
