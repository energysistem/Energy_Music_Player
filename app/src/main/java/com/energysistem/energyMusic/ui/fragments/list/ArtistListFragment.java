
package com.energysistem.energyMusic.ui.fragments.list;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.ui.adapters.list.ArtistListAdapter;
import com.energysistem.energyMusic.ui.fragments.base.ListViewFragment;

import static com.energysistem.energyMusic.Constants.TYPE_ARTIST;

public class ArtistListFragment extends ListViewFragment {

    public ArtistListFragment() {

    }
	
    public ArtistListFragment(Bundle args) {
        setArguments(args);
    }

	@Override
    public void setupFragmentData(){
        mAdapter = new ArtistListAdapter(getActivity(), R.layout.listview_items, null,
                								new String[] {}, new int[] {}, 0);
    	mProjection = new String[] {
                BaseColumns._ID, MediaColumns.TITLE, AudioColumns.ALBUM, AudioColumns.ARTIST
        };
        StringBuilder where = new StringBuilder();
        where.append(AudioColumns.IS_MUSIC + "=1")
        					.append(" AND " + MediaColumns.TITLE + " != ''");
        long artist_id = getArguments().getLong(BaseColumns._ID);
        where.append(" AND " + AudioColumns.ARTIST_ID + "=" + artist_id);
        mWhere = where.toString();        
        mSortOrder = MediaColumns.TITLE;
        mUri = Audio.Media.EXTERNAL_CONTENT_URI;
        mFragmentGroupId = 88;
        mType = TYPE_ARTIST;
        mTitleColumn = MediaColumns.TITLE; 
    }
}
