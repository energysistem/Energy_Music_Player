
package com.energysistem.energyMusic.ui.fragments.list;

import android.os.AsyncTask;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.helpers.utils.MusicUtils;
import com.energysistem.energyMusic.ui.adapters.list.NowPlayingAdapter;
import com.energysistem.energyMusic.ui.fragments.base.DragSortListViewFragment;
import static com.energysistem.energyMusic.Constants.TYPE_SONG;

public class NowPlayingFragment extends DragSortListViewFragment{

    private MatrixCursor playlistCursor;
    private Loader<Cursor> loader;

	@Override
	public void setupFragmentData() {
		mAdapter = new NowPlayingAdapter(getActivity(), R.layout.dragsort_listview_items, null,
		        							new String[] {}, new int[] {}, 0);
		mProjection = new String[] {
		            BaseColumns._ID, MediaColumns.TITLE, AudioColumns.ALBUM, AudioColumns.ARTIST
		    };
		mSortOrder = Audio.Media.DEFAULT_SORT_ORDER;
		mUri = Audio.Media.EXTERNAL_CONTENT_URI;
		StringBuilder where = new StringBuilder();
		long[] mNowPlaying = MusicUtils.getQueue();
		if (mNowPlaying.length <= 0){
			where.append(AudioColumns.IS_MUSIC + "=1").append(" AND " + MediaColumns.TITLE + " != ''");
		}
		else{
			where.append(BaseColumns._ID + " IN (");
			for (long queue_id : mNowPlaying) {
			    where.append(queue_id + ",");
			}
			where.deleteCharAt(where.length() - 1);
			where.append(")");			
		}
		mWhere = where.toString();
        mMediaIdColumn = BaseColumns._ID;
        mType = TYPE_SONG;
        mFragmentGroupId = 91;
	}

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        if (data == null) {
            return;
        }
        loader = cursorLoader;
    	String[] audioCols = new String[] { BaseColumns._ID, MediaColumns.TITLE, AudioColumns.ARTIST, AudioColumns.ALBUM};
        playlistCursor = new MatrixCursor(audioCols);

        LoadItemsTask loadItemTask = new LoadItemsTask();
        loadItemTask.execute(data);
    }

    /**
     * @param which
     */
	@Override
    public void removePlaylistItem(int which) {
        mCursor.moveToPosition(which);
        long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(BaseColumns._ID));
        MusicUtils.removeTrack(id);
        reloadQueueCursor();
        mListView.invalidateViews();
    }

    /**
     * Reload the queue after we remove a track
     */
    private void reloadQueueCursor() {
        String[] cols = new String[] {
                BaseColumns._ID, MediaColumns.TITLE, MediaColumns.DATA, AudioColumns.ALBUM,
                AudioColumns.ARTIST, AudioColumns.ARTIST_ID
        };
        StringBuilder selection = new StringBuilder();
        selection.append(AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaColumns.TITLE + " != ''");
        Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
        long[] mNowPlaying = MusicUtils.getQueue();
        if (mNowPlaying.length == 0) {
        }
        selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < mNowPlaying.length; i++) {
            selection.append(mNowPlaying[i]);
            if (i < mNowPlaying.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
		if(mCursor != null)
			mCursor.close();
        mCursor = MusicUtils.query(getActivity(), uri, cols, selection.toString(), null, null);
        String[] audioCols = new String[] { BaseColumns._ID, MediaColumns.TITLE, AudioColumns.ARTIST, AudioColumns.ALBUM}; 
        MatrixCursor playlistCursor = new MatrixCursor(audioCols);
    	for(int i = 0; i < mNowPlaying.length; i++){
    		mCursor.moveToPosition(-1);
    		while (mCursor.moveToNext()) {
                long audioid = mCursor.getLong(mCursor.getColumnIndexOrThrow(BaseColumns._ID));
            	if( audioid == mNowPlaying[i]) {
                    String trackName = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaColumns.TITLE));
                    String artistName = mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST));
                    String albumName = mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ALBUM));
            		playlistCursor.addRow(new Object[] {audioid, trackName, artistName ,albumName});

            	}
            }
    	}
		if(mCursor != null)
			mCursor.close();
        mCursor = playlistCursor;
        mAdapter.changeCursor(playlistCursor);
    }

    private class LoadItemsTask extends AsyncTask<Cursor, String, Long> {

        long[] mNowPlaying;
        Cursor data;

        @Override
        protected void onPreExecute() {
            mNowPlaying = MusicUtils.getQueue();
        }

        protected Long doInBackground(Cursor... datas) {
            data = datas[0];
            for(int i = 0; i < mNowPlaying.length; i++) {
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    long audioid = data.getLong(data.getColumnIndexOrThrow(BaseColumns._ID));
                    if (audioid == mNowPlaying[i]) {
                        String trackName = data.getString(data.getColumnIndexOrThrow(MediaColumns.TITLE));
                        String artistName = data.getString(data.getColumnIndexOrThrow(AudioColumns.ARTIST));
                        String albumName = data.getString(data.getColumnIndexOrThrow(AudioColumns.ALBUM));
                        publishProgress(String.valueOf(audioid), trackName, artistName, albumName);

                    }
                }
            }
            return Long.parseLong("1");
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            NowPlayingFragment.this.playlistCursor.addRow(new Object[] {progress[0], progress[1], progress[2], progress[3] });
        }

        protected void onPostExecute(Long result) {
            data.close();
            mCursor = playlistCursor;
            NowPlayingFragment.super.onLoadFinished(NowPlayingFragment.this.loader, playlistCursor);
        }
    }
}
