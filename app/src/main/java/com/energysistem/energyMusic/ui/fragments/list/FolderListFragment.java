
package com.energysistem.energyMusic.ui.fragments.list;

import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.ui.adapters.list.GenreListAdapter;
import com.energysistem.energyMusic.ui.fragments.base.ListViewFragment;

import static com.energysistem.energyMusic.Constants.FOLDER_KEY;
import static com.energysistem.energyMusic.Constants.TYPE_GENRE;

public class FolderListFragment extends ListViewFragment  {

    public FolderListFragment() {

    }

    public FolderListFragment(Bundle args) {
        setArguments(args);
    }


    @Override
    public void setupFragmentData(){
        mAdapter = new GenreListAdapter(getActivity(), R.layout.listview_items, null,
                new String[] {}, new int[] {}, 0);
        mProjection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.TITLE, AudioColumns.ALBUM,
                AudioColumns.ARTIST
        };
        String path = getArguments().getString(FOLDER_KEY);
        StringBuilder where = new StringBuilder();
        where.append(AudioColumns.IS_MUSIC + "=1").append(
                " AND " + MediaStore.MediaColumns.TITLE + " != '' AND "+MediaStore.Audio.Media.DATA+" like '"+path.replace("'", "''")+"%'");
        mWhere = where.toString();
        mSortOrder = MediaStore.MediaColumns.TITLE;
        mUri = MediaStore.Audio.Media.getContentUriForPath(path);
        mType = TYPE_GENRE;
        mTitleColumn = MediaStore.MediaColumns.TITLE;
    }
}
