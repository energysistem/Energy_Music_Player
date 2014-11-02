
package com.energysistem.energyMusic.ui.adapters.list;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import com.energysistem.energyMusic.helpers.utils.MusicUtils;
import com.energysistem.energyMusic.ui.adapters.base.ListViewAdapter;

import static com.energysistem.energyMusic.Constants.TYPE_ARTIST;

public class SonglistAdapter extends ListViewAdapter {

    public SonglistAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    public void setupViewData( Cursor mCursor ){
    	mLineOneText = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaColumns.TITLE));
    	
    	mLineTwoText = mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST));
    	
        mImageData = new String[]{ mLineTwoText };
        
        mPlayingId = MusicUtils.getCurrentAudioId();
        mCurrentId = mCursor.getLong(mCursor.getColumnIndexOrThrow(BaseColumns._ID));
        
        mListType = TYPE_ARTIST;   	
    }

}
