
package com.energysistem.energyMusic.ui.fragments.list;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.helpers.Folder;
import com.energysistem.energyMusic.helpers.utils.ApolloUtils;
import com.energysistem.energyMusic.ui.adapters.list.FolderAdapter;
import com.energysistem.energyMusic.ui.fragments.base.ListViewFragment;

import java.util.ArrayList;

import static com.energysistem.energyMusic.Constants.TYPE_FOLDER;

public class FolderFragment extends ListViewFragment  {

    private FolderAdapter folderAdapter;

    public FolderFragment() {

    }

    public FolderFragment(Bundle args) {
        setArguments(args);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupFragmentData();
        setUpBroadcastReceiver();
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(folderAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void setupFragmentData(){
        folderAdapter = new FolderAdapter(getActivity(), R.layout.listview_items);
        mProjection = new String[] {
                MediaStore.Audio.Media.DATA
        };
        StringBuilder where = new StringBuilder();
        where.append(AudioColumns.IS_MUSIC + "=1").append(" AND " + MediaStore.Audio.Media.DATA + " != ''");
        mWhere = where.toString();
        mSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mType = TYPE_FOLDER;
        mTitleColumn = MediaStore.Audio.Media.DATA;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        // Check for database errors
        if (data == null) {
            return;
        }

        ArrayList<String> dataArray = new ArrayList<String>();
        data.moveToFirst();
        do {
            String path = data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            Folder folder = new Folder(path.substring(0, path.lastIndexOf("/")));
            if(!dataArray.contains(folder.getPath())) {
                dataArray.add(folder.getPath());
                folderAdapter.add(folder);
            }
        }while(data.moveToNext());
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (folderAdapter != null)
            folderAdapter.clear();
    }

    @Override
    protected void setUpBroadcastReceiver() {
        mMediaStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mListView != null) {
                    folderAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        mCursor.moveToFirst();
        String folderPath = folderAdapter.getItem(position).getPath();
        String cursorPath = "";
        do {
            cursorPath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            cursorPath = cursorPath.substring(0, cursorPath.lastIndexOf("/"));
            if(folderPath.equals(cursorPath))
                break;
        }while(mCursor.moveToNext());
        ApolloUtils.startTracksBrowser(mType, id, mCursor, getActivity());
    }
}
