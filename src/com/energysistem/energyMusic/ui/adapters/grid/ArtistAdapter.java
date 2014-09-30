
package com.energysistem.energyMusic.ui.adapters.grid;

import static com.energysistem.energyMusic.Constants.TYPE_ARTIST;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;

import com.energysistem.energyMusic.helpers.utils.MusicUtils;
import com.energysistem.energyMusic.ui.adapters.base.GridViewAdapter;

public class ArtistAdapter extends GridViewAdapter {

    public ArtistAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    public void setupViewData(Cursor mCursor){

    	mLineOneText = mCursor.getString(mCursor.getColumnIndexOrThrow(ArtistColumns.ARTIST));
        int albums_plural = mCursor.getInt(mCursor.getColumnIndexOrThrow(ArtistColumns.NUMBER_OF_ALBUMS));
        int songs_plural = mCursor.getInt(mCursor.getColumnIndexOrThrow(ArtistColumns.NUMBER_OF_TRACKS));
        boolean unknown = mLineOneText == null || mLineOneText.equals(MediaStore.UNKNOWN_STRING);
        mLineTwoText = MusicUtils.makeAlbumsLabel(mContext, albums_plural, songs_plural, unknown);
        mGridType = TYPE_ARTIST;        
        mImageData = new String[]{mLineOneText};
        mPlayingId = MusicUtils.getCurrentArtistId();
        mCurrentId = mCursor.getLong(mCursor.getColumnIndexOrThrow(BaseColumns._ID));
        
    }
}
