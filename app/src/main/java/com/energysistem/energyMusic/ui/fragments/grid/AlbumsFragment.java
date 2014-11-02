
package com.energysistem.energyMusic.ui.fragments.grid;

import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AlbumColumns;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.ui.adapters.grid.AlbumAdapter;
import com.energysistem.energyMusic.ui.fragments.base.GridViewFragment;

import static com.energysistem.energyMusic.Constants.TYPE_ALBUM;

public class AlbumsFragment extends GridViewFragment {

    public void setupFragmentData(){
    	mAdapter = new AlbumAdapter(getActivity(), R.layout.gridview_items, null,
                					new String[] {}, new int[] {}, 0); 
    	mProjection = new String []{
                BaseColumns._ID, AlbumColumns.ALBUM, AlbumColumns.ARTIST, AlbumColumns.ALBUM_ART
        };
        mUri = Audio.Albums.EXTERNAL_CONTENT_URI;
        mSortOrder = Audio.Albums.DEFAULT_SORT_ORDER;
        mFragmentGroupId = 2;
        mType = TYPE_ALBUM;
    }
    
}
