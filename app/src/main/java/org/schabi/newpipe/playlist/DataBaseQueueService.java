package org.schabi.newpipe.playlist;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import org.schabi.newpipe.playlist.dao.PlayListDAO;
import org.schabi.newpipe.playlist.dto.PlayList;
import org.schabi.newpipe.playlist.dto.PlayListStreamInfo;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in a service on a separate handler thread.
 * helper methods for manipulate the playlist
 */
public class DataBaseQueueService extends IntentService {

    private static final String TAG = DataBaseQueueService.class.getName();
    // actions
    private static final String ACTION_RETRIEVE_NEXT_ITEMS_ON_PLAYLIST = TAG + ".action.NEXT_ITEMS_ON_PLAYLIST";
    private static final String ACTION_RETRIEVE_PREVIOUS_ITEMS_ON_PLAYLIST = TAG + ".action.PREVIOUS_ITEMS_ON_PLAYLIST";
    private static final String ACTION_RETRIEVE_RANDOM_ITEMS_ON_PLAYLIST = TAG + ".action.RETRIEVE_RANDOM_ITEMS_ON_PLAYLIST";
    private static final String ACTION_ADD_ITEMS_ON_PLAYLIST = TAG + ".action.ADD_ITEMS_ON_PLAYLIST";
    private static final String ACTION_REMOVE_ITEMS_ON_PLAYLIST = TAG + ".action.REMOVE_ITEMS_ON_PLAYLIST";
    private static final String ACTION_SWAP_ITEMS_ON_PLAYLIST = TAG + ".action.SWAP_ITEMS_ON_PLAYLIST";
    private static final String ACTION_CREATE_PLAYLIST = TAG + ".action.CREATE_PLAYLIST";
    private static final String ACTION_REMOVE_PLAYLIST = TAG + ".action.REMOVE_PLAYLIST";
    // parameters
    private static final String EXTRA_PLAYLIST_ID = TAG + ".extra.PLAYLIST_ID";
    private static final String EXTRA_PLAYLIST_NAME = TAG + ".extra.PLAYLIST_NAME";
    private static final String EXTRA_CURRENT_MEDIA = TAG + ".extra.CURRENT_URL";
    private static final String EXTRA_POSITION = TAG + ".extra.POSITION";
    private static final String EXTRA_PLAYLIST_ITEM = TAG + ".extra.PLAYLIST_ITEM";
    public static final String EXTRA_SUCCESS = TAG +".extra.SUCCESS";

    public static final int DATABASE_BUNDLE_RESPONSE = TAG.hashCode();
    public static final String RESULT_RECEIVER_CREATE_PLAYLIST = TAG + ".receiver.create_playlist";
    public static final String RESULT_RECEIVER_DELETE_PLAYLIST = TAG + ".receiver.remove_playlist";
    public static final String RESULT_RECEIVER_NEXT_ITEMS_ON_PLAYLIST = TAG + ".receiver.next_items_on_playlist";
    public static final String RESULT_RECEIVER_PREVIOUS_ITEMS_ON_PLAYLIST = TAG + ".receiver.previous_items_on_playlist";
    public static final String RESULT_RECEIVER_RANDOM_ITEMS_ON_PLAYLIST = TAG + ".receiver.previous_items_on_playlist";
    public static final String RESULT_RECEIVER_SWAP_ITEM_PLAYLIST = TAG + ".receiver.swap_items_on_playlist";
    public static final String RESULT_RECEIVER_ADD_ITEM_PLAYLIST = TAG + ".receiver.add_items_on_playlist";
    public static final String RESULT_RECEIVER_DELETE_ITEM_PLAYLIST = TAG + ".receiver.remove_items_on_playlist";
    public static final String RESULT_RECEIVER_RECEIVER = TAG + ".receiver.receiver";

    public static final int NO_VALUE = -9999;

    public DataBaseQueueService() {
        super("DataBaseQueueService");
    }


    /**
     * Starts this service to perform action create a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread
     * @param playlistName the playlist name
     */
    public static void handleCreatePlayList(final Context context, final ResultReceiver receiver, final String playlistName) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_CREATE_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_NAME, playlistName);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action delete a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread
     * @param playlistId the playlist id
     */
    public static void deletePlayList(final Context context, final ResultReceiver receiver, final int playlistId) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_REMOVE_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_NAME, playlistId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action add medias from a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread
     * @param playlistId the playlist id
     * @param playListStreamInfo the playList stream info
     */
    public static void addItemToPlayList(final Context context, final ResultReceiver receiver, final int playlistId, final PlayListStreamInfo playListStreamInfo) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_ADD_ITEMS_ON_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
        final Bundle resultData = new Bundle();
        resultData.putSerializable(EXTRA_CURRENT_MEDIA, playListStreamInfo);
        intent.putExtra(EXTRA_CURRENT_MEDIA, resultData);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action remove medias from a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread
     * @param playlistId the playlist id
     * @param position the index on the playlist
     */
    public static void removeItemToPlayList(final Context context, final ResultReceiver receiver, final int playlistId, final int position) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_REMOVE_ITEMS_ON_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
        intent.putExtra(EXTRA_POSITION, position);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action retrieve next medias from a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread=
     * @param playlistId the playlist id
     * @param currentPosition The current position in the playlist
     */
    public static void retrieveNextMediaFromPlayList(final Context context, final ResultReceiver receiver, final int playlistId, final int currentPosition) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_RETRIEVE_NEXT_ITEMS_ON_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
        intent.putExtra(EXTRA_POSITION, currentPosition);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action retrieve previous medias from a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread
     * @param playlistId the playlist id
     * @param currentPosition The current position in the playlist
     */
    public static void retrievePreviousMediaFromPlayList(final Context context, final ResultReceiver receiver, final int playlistId, final int currentPosition) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_RETRIEVE_NEXT_ITEMS_ON_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
        intent.putExtra(EXTRA_POSITION, currentPosition);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action retrieve random medias from a playlist.
     * If the service is already performing a task this action will be queued.
     * @param context Use to provide an parent for lunch the background task
     * @param receiver Use to provide update info to ui thread
     * @param playlistId the playlist id
     */
    public static void retrieveRandomMediaFromPlayList(final Context context, final ResultReceiver receiver, final int playlistId) {
        final Intent intent = new Intent(context, DataBaseQueueService.class);
        intent.setAction(ACTION_RETRIEVE_RANDOM_ITEMS_ON_PLAYLIST);
        intent.putExtra(RESULT_RECEIVER_RECEIVER, receiver);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_CREATE_PLAYLIST.equals(action)) {
                handleCreatePlayList(intent);
            } else if(ACTION_REMOVE_PLAYLIST.equals(action)) {
                handleDeletePlayList(intent);
            } else if(ACTION_ADD_ITEMS_ON_PLAYLIST.equals(action)) {
                handleAddItemOnPlayList(intent);
            } else if(ACTION_REMOVE_ITEMS_ON_PLAYLIST.equals(action)) {
                handleDeleteItemOnPlayList(intent);
            } else if (ACTION_RETRIEVE_NEXT_ITEMS_ON_PLAYLIST.equals(action)) {
                handleRetrieveNextItemOnPlayList(intent);
            } else if (ACTION_RETRIEVE_PREVIOUS_ITEMS_ON_PLAYLIST.equals(action)) {
                handleRetrievePreviousItemOnPlayList(intent);
            } else if (ACTION_RETRIEVE_RANDOM_ITEMS_ON_PLAYLIST.equals(action)) {
                handleRetrieveRandomItemOnPlayList(intent);
            }
        }
    }

    private void handleRetrieveRandomItemOnPlayList(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_RANDOM_ITEMS_ON_PLAYLIST);
        final int playListId = getPlayListId(intent);
        final Bundle resultData = new Bundle();
        PlayListStreamInfo playListStreamInfo = null;
        if(playListId != NO_VALUE) {
            final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
            playListStreamInfo = playListDAO.getRandomItem(playListId);
        }
        resultData.putSerializable(EXTRA_PLAYLIST_ITEM, playListStreamInfo);
        resultData.putBoolean(EXTRA_SUCCESS, playListStreamInfo != null);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private void handleRetrievePreviousItemOnPlayList(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_PREVIOUS_ITEMS_ON_PLAYLIST);
        final int position = intent.getIntExtra(EXTRA_POSITION, NO_VALUE);
        final int playListId = getPlayListId(intent);
        final Bundle resultData = new Bundle();
        PlayListStreamInfo playListStreamInfo = null;
        if(position != NO_VALUE && playListId != NO_VALUE) {
            final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
            playListStreamInfo = playListDAO.getPreviousEntryForItems(playListId, position);
        }
        resultData.putSerializable(EXTRA_PLAYLIST_ITEM, playListStreamInfo);
        resultData.putBoolean(EXTRA_SUCCESS, playListStreamInfo != null);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private void handleRetrieveNextItemOnPlayList(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_NEXT_ITEMS_ON_PLAYLIST);
        final int position = intent.getIntExtra(EXTRA_POSITION, NO_VALUE);
        final int playListId = getPlayListId(intent);
        final Bundle resultData = new Bundle();
        PlayListStreamInfo playListStreamInfo = null;
        if(position != NO_VALUE && playListId != NO_VALUE) {
            final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
            playListStreamInfo = playListDAO.getNextEntryForItems(playListId, position);
        }
        resultData.putSerializable(EXTRA_PLAYLIST_ITEM, playListStreamInfo);
        resultData.putBoolean(EXTRA_SUCCESS, playListStreamInfo != null);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private void handleDeleteItemOnPlayList(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_DELETE_ITEM_PLAYLIST);
        final int position = intent.getIntExtra(EXTRA_POSITION, NO_VALUE);
        final int playListId = getPlayListId(intent);
        final Bundle resultData = new Bundle();
        boolean success = false;
        if(position != NO_VALUE && playListId != NO_VALUE) {
            final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
            final int result = playListDAO.deleteEntryFromPlayList(playListId, position);
            success = result > 0;
        }
        resultData.putBoolean(EXTRA_SUCCESS, success);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private void handleAddItemOnPlayList(final Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_ADD_ITEM_PLAYLIST);
        final int playlistId = getPlayListId(intent);
        final PlayListStreamInfo playlistStreamInfos = (PlayListStreamInfo) intent.getBundleExtra(EXTRA_CURRENT_MEDIA).getSerializable(EXTRA_CURRENT_MEDIA);

        final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
        final long entryId = playListDAO.addEntryToPlayList(playlistId, playlistStreamInfos);
        // notify main thread with playlist
        final Bundle resultData = new Bundle();
        resultData.putLong(RESULT_RECEIVER_CREATE_PLAYLIST, entryId);
        resultData.putBoolean(EXTRA_SUCCESS, entryId > 0);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private void handleCreatePlayList(final Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_CREATE_PLAYLIST);
        final String playlistName = intent.getStringExtra(EXTRA_PLAYLIST_NAME);
        final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
        final PlayList playlist = playListDAO.createPlayList(playlistName);
        // notify main thread with playlist
        final Bundle resultData = new Bundle();
        resultData.putSerializable(RESULT_RECEIVER_CREATE_PLAYLIST, playlist);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private void handleDeletePlayList(final Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(RESULT_RECEIVER_DELETE_PLAYLIST);
        final int playlistId = getPlayListId(intent);
        if(playlistId > -1) {
            final PlayListDAO playListDAO = new PlayListDAO(getApplicationContext());
            playListDAO.deletePlayList(playlistId);
        }
        final Bundle resultData = new Bundle();
        resultData.putBoolean(EXTRA_SUCCESS, true);
        receiver.send(DATABASE_BUNDLE_RESPONSE, resultData);
    }

    private int getPlayListId(Intent intent) {
        return intent.getIntExtra(EXTRA_PLAYLIST_NAME, NO_VALUE);
    }
}