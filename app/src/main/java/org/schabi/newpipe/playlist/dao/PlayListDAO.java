package org.schabi.newpipe.playlist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.schabi.newpipe.playlist.dto.PlayList;
import org.schabi.newpipe.playlist.dto.PlayListStreamInfo;
import org.schabi.newpipe.playlist.dao.NewPipeSQLiteHelper.PLAYLIST_COLUMNS;
import org.schabi.newpipe.playlist.dao.NewPipeSQLiteHelper.PLAYLIST_LINK_ENTRIES;

import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.playlist.dao.NewPipeSQLiteHelper.PLAYLIST_ENTRIES_COLUMNS;
import static org.schabi.newpipe.playlist.dao.NewPipeSQLiteHelper.Qualified;
import static org.schabi.newpipe.playlist.dao.NewPipeSQLiteHelper.Tables;
import static org.schabi.newpipe.playlist.dao.NewPipeSQLiteHelper.concat;


public class PlayListDAO {

    private final String TAG = PlayListDAO.class.getName();

    private SQLiteDatabase database;
    private NewPipeSQLiteHelper dbHelper;

    public interface PLAYLIST_SYSTEM {
        int POSITION_DEFAULT = 0;
        int NOT_IN_PLAYLIST_ID = -1;
        String HISTORIC = "historic";
        int HISTORIC_ID = -2;
        String FAVORITES = "favorites";
        int FAVORITES_ID = -3;
        String QUEUE = "queue";
        int QUEUE_ID = -4;
    }

    public PlayListDAO(final Context context) {
        dbHelper = new NewPipeSQLiteHelper(context);
    }

    private void open() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
    }

    private void close() {
        if (database != null && database.isOpen()) {
            dbHelper.close();
        }
    }

    /**
     * Use to clone a playlist to and other playlist
     *
     * @param playlistIdFrom playlist source
     * @param playlistIdTo   playlist destination
     */
    public void duplicatePlayListAOnPlaylistB(final int playlistIdFrom, final int playlistIdTo) {
        final long lastPosition = getNumberOfEntriesOnPlayList(playlistIdTo);
        String sb = "INSERT INTO " +
                Tables.PLAYLIST_LINK_ENTRIES +
                "(" +
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + ", " +
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ENTRIES_ID + ", " +
                PLAYLIST_LINK_ENTRIES.POSITION +
                ")" +
                " SELECT " +
                playlistIdTo + ", " +
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ENTRIES_ID + ", " +
                PLAYLIST_LINK_ENTRIES.POSITION + "+" + lastPosition +
                " FROM " +
                Tables.PLAYLIST_LINK_ENTRIES +
                " WHERE " +
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + " = " + playlistIdFrom;
        open();
        database.execSQL(sb);
        close();
        Log.d(TAG, "Duplicate playlist : " + playlistIdFrom + " to " + playlistIdTo + " : " + sb);
    }

    /**
     * Use to chack is a custom playlist exist (created by the user)
     *
     * @return boolean
     */
    public boolean hasPersonalPlayList() {
        open();
        long nb = DatabaseUtils.queryNumEntries(database, Tables.PLAYLIST, PLAYLIST_COLUMNS.PLAYLIST_SYSTEM + "=?", new String[]{"0"});
        close();
        return nb > 0;
    }

    /**
     * Retrieve the name of a playlist for a given playlist id
     *
     * @param playlistId the playlist id
     * @return the playlist name
     */
    public String getPlaylistName(final int playlistId) {
        open();
        final Cursor cursor = database.query(Tables.PLAYLIST,
                new String[]{PLAYLIST_COLUMNS.PLAYLIST_NAME},
                PLAYLIST_COLUMNS._ID + "=?",
                new String[]{String.valueOf(playlistId)},
                null, null, null);
        cursor.moveToFirst();
        final String name = cursor.getString(cursor.getColumnIndex(PLAYLIST_COLUMNS.PLAYLIST_NAME));
        cursor.close();
        close();
        return name;
    }

    /**
     * Retrieve the id of a playlist for a given playlist namr
     *
     * @param name the playlist name
     * @return the playlist id
     */
    public int getPlayListId(final String name) {
        open();
        final Cursor cursor = database.query(Tables.PLAYLIST,
                new String[]{PLAYLIST_COLUMNS._ID},
                PLAYLIST_COLUMNS.PLAYLIST_NAME + "=?",
                new String[]{name},
                null, null, null);
        int id = -1;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            id = cursor.getInt(0);
        }
        cursor.close();
        close();
        return id;
    }

    /**
     * Retrieve a random entry from a playlist
     *
     * @param playlistId the playlist id
     * @return the random PlaylistStreamInfo found
     */
    public PlayListStreamInfo getRandomItem(final int playlistId) {
        open();
        Cursor cursor = database.query(Tables.PLAYLIST_LINK_JOIN_ENTRIES,
                concat(PLAYLIST_ENTRIES_COLUMNS.ALL_COLUMNS, new String[]{PLAYLIST_LINK_ENTRIES.POSITION}),
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=?",
                new String[]{String.valueOf(playlistId)},
                null,
                null,
                "RANDOM()",
                "1");
        final PlayListStreamInfo stream;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            stream = getPlayListStreamInfo(cursor);
        } else {
            stream = null;
        }
        cursor.close();
        close();
        return stream;
    }

    /**
     * Retrieve the previous entry from a playlist
     *
     * @param playlistId the playlist id
     * @param position   the current position on the playlist
     * @return The previous item, it's return null if no item was found
     */
    public PlayListStreamInfo getPreviousEntryForItems(final int playlistId, final int position) {
        open();
        final Cursor cursor = database.query(Tables.PLAYLIST_LINK_JOIN_ENTRIES,
                concat(PLAYLIST_ENTRIES_COLUMNS.ALL_COLUMNS, new String[]{PLAYLIST_LINK_ENTRIES.POSITION}),
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=? AND " +
                        PLAYLIST_LINK_ENTRIES.POSITION + "<?",
                new String[]{
                        String.valueOf(playlistId),
                        String.valueOf(position)
                },
                null,
                null,
                PLAYLIST_LINK_ENTRIES.POSITION + " DESC",
                "1");
        final PlayListStreamInfo stream;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            stream = getPlayListStreamInfo(cursor);
        } else {
            stream = null;
        }
        cursor.close();
        close();
        return stream;
    }

    /**
     * Retrieve the next entry from a playlist
     *
     * @param playlistId the playlist id
     * @param position   the current position on the playlist
     * @return The next item, it's return null if no item was found
     */
    public PlayListStreamInfo getNextEntryForItems(final int playlistId, final int position) {
        open();
        final Cursor cursor = database.query(Tables.PLAYLIST_LINK_JOIN_ENTRIES,
                concat(PLAYLIST_ENTRIES_COLUMNS.ALL_COLUMNS, new String[]{PLAYLIST_LINK_ENTRIES.POSITION}),
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=? AND " +
                        PLAYLIST_LINK_ENTRIES.POSITION + ">?",
                new String[]{
                        String.valueOf(playlistId),
                        String.valueOf(position)
                }, null, null,
                PLAYLIST_LINK_ENTRIES.POSITION + " ASC", "1");
        final PlayListStreamInfo stream;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            stream = getPlayListStreamInfo(cursor);
        } else {
            stream = null;
        }
        cursor.close();
        close();
        return stream;
    }

    /**
     * Retrieve the entry from a playlist for a given position
     *
     * @param playlistId the playlist id
     * @param position   the position on the playlist
     * @return The item found, it's return null if no item was found
     */
    public PlayListStreamInfo getEntryForItems(final int playlistId, final int position) {
        open();
        final Cursor cursor = database.query(Tables.PLAYLIST_LINK_JOIN_ENTRIES,
                concat(PLAYLIST_ENTRIES_COLUMNS.ALL_COLUMNS, new String[]{PLAYLIST_LINK_ENTRIES.POSITION}),
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=? AND" +
                        PLAYLIST_LINK_ENTRIES.POSITION + "=?",
                new String[]{
                        String.valueOf(playlistId),
                        String.valueOf(position)
                }, null, null,
                PLAYLIST_LINK_ENTRIES.POSITION + " ASC", "1");
        final PlayListStreamInfo stream;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            stream = getPlayListStreamInfo(cursor);
        } else {
            stream = null;
        }
        cursor.close();
        close();
        return stream;
    }


    private PlayListStreamInfo getEntryForPlayList(final int playlistId, final boolean orderAsc) {
        open();
        final String ORDER = orderAsc ? " ASC" : " DESC";
        final String[] columns = concat(PLAYLIST_ENTRIES_COLUMNS.ALL_COLUMNS, PLAYLIST_LINK_ENTRIES.ALL_COLUMNS);
        final Cursor cursor = database.query(Tables.PLAYLIST_LINK_JOIN_ENTRIES,
                columns,
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)}, null, null,
                PLAYLIST_LINK_ENTRIES.POSITION + ORDER, "1");
        final PlayListStreamInfo stream;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            stream = getPlayListStreamInfo(cursor);
        } else {
            stream = null;
        }
        cursor.close();
        close();
        return stream;
    }

    /**
     * Retrieve the first item for a playlist
     *
     * @param playlistId the playlist id
     * @return The item found, it's return null if no item was found
     */
    public PlayListStreamInfo getFirstEntryForPlayList(final int playlistId) {
        return getEntryForPlayList(playlistId, true);
    }


    /**
     * Retrieve the last item for a playlist
     *
     * @param playlistId the playlist id
     * @return The item found, it's return null if no item was found
     */
    public PlayListStreamInfo getLastEntryForPlayList(final int playlistId) {
        return getEntryForPlayList(playlistId, false);
    }

    /**
     * Create a playlist and retrieve it
     *
     * @param name the playlist name
     * @return the playlist
     */
    public PlayList createPlayList(final String name) {
        open();
        final ContentValues values = new ContentValues();
        values.put(PLAYLIST_COLUMNS.PLAYLIST_NAME, name);
        values.put(PLAYLIST_COLUMNS.PLAYLIST_SYSTEM, 0);
        final long insertId = database.insert(Tables.PLAYLIST, null, values);
        final Cursor cursor = database.query(Tables.PLAYLIST, PLAYLIST_COLUMNS.ALL_COLUMNS, PLAYLIST_COLUMNS._ID + "=?", new String[]{String.valueOf(insertId)}, null, null, null);
        cursor.moveToFirst();
        final PlayList playList = cursorToPlayList(cursor);
        cursor.close();
        close();
        return playList;
    }

    /**
     * Delete the playlist
     *
     * @param id the playlist id
     */
    public void deletePlayList(final int id) {
        open();
        // For entry it's delete by the trigger
        Log.i(TAG, "Delete playlist with id : " + id);
        database.delete(Tables.PLAYLIST, PLAYLIST_COLUMNS._ID + "=?", new String[]{String.valueOf(id)});
        Log.i(TAG, "Deleted playlist with id : " + id);
        close();
    }

    /**
     * Return the number of entries inside a playlist
     *
     * @param playlistId the playlist id
     * @return the number of entries
     */
    public long getNumberOfEntriesOnPlayList(final int playlistId) {
        open();
        long nb = DatabaseUtils.queryNumEntries(database, Tables.PLAYLIST_LINK_ENTRIES, PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)});
        close();
        return nb;
    }

    /**
     * records entries to a given playlist
     *
     * @param playListId the playlist id
     * @param streams    the entries to records
     */
    public void addEntriesToPlayList(final int playListId, final List<PlayListStreamInfo> streams) {
        if (streams != null && !streams.isEmpty()) {
            for (int i = 0; i < streams.size(); i++) {
                addEntryToPlayList(playListId, streams.get(i));
            }
        }
    }

    /**
     * Record entry to a given playlist
     *
     * @param playListId the playlist id
     * @param info       the entry to records
     * @return the entry id
     */
    public long addEntryToPlayList(final int playListId, final PlayListStreamInfo info) {
        // first add entry
        // first check if values is already existing in data base
        long entryId = getEntryId(info.id, info.service_id);
        final ContentValues values = new ContentValues();
        if (entryId < 0) {
            open();
            values.put(PLAYLIST_ENTRIES_COLUMNS.SERVICE_ID, info.service_id);
            values.put(PLAYLIST_ENTRIES_COLUMNS.ID, info.id);
            values.put(PLAYLIST_ENTRIES_COLUMNS.TITLE, info.title);
            values.put(PLAYLIST_ENTRIES_COLUMNS.UPLOADER, info.uploader);
            values.put(PLAYLIST_ENTRIES_COLUMNS.THUMBNAIL_URL, info.thumbnail_url);
            values.put(PLAYLIST_ENTRIES_COLUMNS.WEBPAGE_URL, info.webpage_url);
            values.put(PLAYLIST_ENTRIES_COLUMNS.UPLOAD_DATE, info.upload_date);
            values.put(PLAYLIST_ENTRIES_COLUMNS.VIEW_COUNT, info.view_count);
            values.put(PLAYLIST_ENTRIES_COLUMNS.DURATION, info.duration);
            entryId = database.insert(Tables.PLAYLIST_ENTRIES, null, values);
        }
        if (entryId > -1) {
            values.clear();
            final long position = getNumberOfEntriesOnPlayList(playListId) + 1;
            values.put(PLAYLIST_LINK_ENTRIES.PLAYLIST_ID, playListId);
            values.put(PLAYLIST_LINK_ENTRIES.PLAYLIST_ENTRIES_ID, entryId);
            values.put(PLAYLIST_LINK_ENTRIES.POSITION, position);
            // because count entries on playlist close the database
            open();
            entryId = database.insert(Tables.PLAYLIST_LINK_ENTRIES, null, values);
            Log.d(TAG, String.format("Insert to playlist (%d) at %d value entry : %s", playListId,
                    position, info.webpage_url));
        }
        close();
        return entryId;
    }

    /**
     * Retrieve the entry id (pk) for given media
     *
     * @param id         the id from the media
     * @param service_id the serviceId use by media
     * @return the entry id
     */
    public long getEntryId(final String id, final int service_id) {
        open();
        final Cursor cursor = database.query(Tables.PLAYLIST_ENTRIES,
                new String[]{Qualified.PLAYLIST_ENTRIES_ID},
                PLAYLIST_ENTRIES_COLUMNS.ID + "=? AND " + PLAYLIST_ENTRIES_COLUMNS.SERVICE_ID + "=?",
                new String[]{id, String.valueOf(service_id)}, null, null, null);
        long entries_id;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            entries_id = cursor.getLong(0);
        } else {
            entries_id = -1;
        }
        cursor.close();
        close();
        return entries_id;
    }

    /**
     * Delete an entry for a playlist
     *
     * @param playlist_id the id of the playlist
     * @param position    the position on the playlist
     * @return the result of the delete
     */
    public int deleteEntryFromPlayList(int playlist_id, int position) {
        open();
        final int result = database.delete(Tables.PLAYLIST_LINK_ENTRIES,
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=? AND " +
                        PLAYLIST_LINK_ENTRIES.POSITION + "=?",
                new String[]{String.valueOf(playlist_id), String.valueOf(position)});
        Log.i(TAG, String.format("Deleted playlist entry with position : %d for playlist : %d", position, playlist_id));
        close();
        return result;
    }


    /**
     * Delete all entries for a playlist
     *
     * @param playlist_id the id of the playlist
     * @return the result of the delete
     */
    public int deleteAllEntryFromPlayList(final int playlist_id) {
        open();
        final int result = database.delete(Tables.PLAYLIST_LINK_ENTRIES, PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=?", new String[]{String.valueOf(playlist_id)});
        close();
        return result;
    }

    /**
     * Retrieve the playlist with entries loaded for a given pagination
     *
     * @param playlist_id the id of the playlist
     * @param page        the given page to retrieve
     * @return the playlist with related entries loaded
     */
    public PlayList getPlayListWithEntries(final int playlist_id, final int page) {
        open();
        final String[] ALL_COLUMNS = concat(concat(PLAYLIST_COLUMNS.ALL_COLUMNS, PLAYLIST_ENTRIES_COLUMNS.ALL_COLUMNS), PLAYLIST_LINK_ENTRIES.ALL_COLUMNS);
        final ArrayList<String> ALL_COLUMNS_USE = new ArrayList<>(ALL_COLUMNS.length);
        for (final String ALL_COLUMN : ALL_COLUMNS) {
            if (!PLAYLIST_COLUMNS._ID.equals(ALL_COLUMN)) {
                ALL_COLUMNS_USE.add(ALL_COLUMN);
            }
        }
        final String limit = page > 0 ? (page * 10) + ",10" : "10";
        final Cursor cursor = database.query(Tables.PLAYLIST_JOIN_PLAYLIST_ENTRIES,
                ALL_COLUMNS_USE.toArray(new String[ALL_COLUMNS_USE.size()]),
                Qualified.PLAYLIST_ID + "=?",
                new String[]{String.valueOf(playlist_id)},
                null,
                null,
                PLAYLIST_LINK_ENTRIES.POSITION + " ASC",
                limit);
        final ArrayList<PlayListStreamInfo> entries = new ArrayList<>(cursor.getCount());
        String name = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (name == null) {
                name = cursor.getString(cursor.getColumnIndex(PLAYLIST_COLUMNS.PLAYLIST_NAME));
            }
            entries.add(getPlayListStreamInfo(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return new PlayList(playlist_id, name, entries);
    }

    /**
     * Return all existing playlist name on database
     *
     * @param includeSystem include system playlist on result
     * @return all playlists found
     */
    public SparseArray<String> getAllPlayList(final boolean includeSystem) {
        open();
        final SparseArray<String> playList = new SparseArray<>();
        final String selection = includeSystem ? null : PLAYLIST_COLUMNS.PLAYLIST_SYSTEM + "=?";
        final String[] selectionArg = includeSystem ? null : new String[]{"0"};
        final Cursor cursor = database.query(Tables.PLAYLIST, PLAYLIST_COLUMNS.ALL_COLUMNS, selection, selectionArg, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            final String name = cursor.getString(cursor.getColumnIndex(PLAYLIST_COLUMNS.PLAYLIST_NAME));
            final int uid = cursor.getInt(cursor.getColumnIndex(PLAYLIST_COLUMNS._ID));
            playList.put(uid, name);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return playList;
    }

    /**
     * Use to update a position of an item inside a playlist
     *
     * @param playListId  the playlist id
     * @param stream      the item
     * @param newPosition the new position
     */
    public void updatePosition(int playListId, PlayListStreamInfo stream, int newPosition) {
        final long entryId = getEntryId(stream.id, stream.service_id);
        final long oldPosition = stream.position;
        open();
        final ContentValues cv = new ContentValues();
        cv.put(PLAYLIST_LINK_ENTRIES.POSITION, newPosition);
        database.update(Tables.PLAYLIST_LINK_ENTRIES, cv,
                PLAYLIST_LINK_ENTRIES.PLAYLIST_ID + "=? AND " +
                        PLAYLIST_LINK_ENTRIES.POSITION + "=? AND " +
                        PLAYLIST_LINK_ENTRIES.PLAYLIST_ENTRIES_ID + "=?",
                new String[]{
                        String.valueOf(playListId),
                        String.valueOf(oldPosition),
                        String.valueOf(entryId)
                });
        Log.d(TAG, String.format("Update position of entriesId %d on playlistId %d: %d -> %d", entryId,
                playListId, oldPosition, newPosition));
        // update to new position item
        stream.position = newPosition;
        close();
    }

    /**
     * Return if a given playlist an a next pagination
     *
     * @param playListId the playlist id
     * @param page       the current page
     * @return the result (boolean)
     */
    public boolean hasNextPage(int playListId, int page) {
        long nbEntries = getNumberOfEntriesOnPlayList(playListId);
        int nbItemViewOnThisPage = page == 0 ? 10 : page * 10;
        return nbEntries - nbItemViewOnThisPage >= 0;
    }

    /**
     * USe to map a cursor to a playlist entry
     *
     * @param cursor the cursor
     * @return the entry
     */
    @NonNull
    private PlayListStreamInfo getPlayListStreamInfo(final Cursor cursor) {
        final PlayListStreamInfo stream = new PlayListStreamInfo();
        stream.service_id = cursor.getInt(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.SERVICE_ID));
        stream.id = cursor.getString(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.ID));
        stream.title = cursor.getString(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.TITLE));
        stream.uploader = cursor.getString(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.UPLOADER));
        stream.thumbnail_url = cursor.getString(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.THUMBNAIL_URL));
        stream.webpage_url = cursor.getString(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.WEBPAGE_URL));
        stream.upload_date = cursor.getString(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.UPLOAD_DATE));
        stream.view_count = cursor.getLong(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.VIEW_COUNT));
        stream.duration = cursor.getInt(cursor.getColumnIndex(PLAYLIST_ENTRIES_COLUMNS.DURATION));
        stream.playlistId = cursor.getInt(cursor.getColumnIndex(PLAYLIST_LINK_ENTRIES.PLAYLIST_ID));
        final int positionColumn = cursor.getColumnIndex(PLAYLIST_LINK_ENTRIES.POSITION);
        if (positionColumn > -1) {
            stream.position = cursor.getInt(positionColumn);
        }
        return stream;
    }

    /**
     * Use to map cursor to playlist
     *
     * @param cursor the cusor
     * @return the playlist
     */
    private PlayList cursorToPlayList(final Cursor cursor) {
        final int _id = cursor.getInt(cursor.getColumnIndex(PLAYLIST_COLUMNS._ID));
        final String playlistName = cursor.getString(cursor.getColumnIndex(PLAYLIST_COLUMNS.PLAYLIST_NAME));
        return new PlayList(_id, playlistName, new ArrayList<PlayListStreamInfo>());
    }

}
