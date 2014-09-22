
package com.energysistem.energyMusic.ui.fragments.list;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.helpers.Folder;
import com.energysistem.energyMusic.helpers.utils.ApolloUtils;
import com.energysistem.energyMusic.helpers.utils.MusicUtils;
import com.energysistem.energyMusic.ui.adapters.list.FolderAdapter;
import com.energysistem.energyMusic.ui.fragments.base.ListViewFragment;

import java.util.ArrayList;
import java.util.Comparator;

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
        mSortOrder = MediaStore.Audio.Media.DATA;
        mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mType = TYPE_FOLDER;
        mTitleColumn = MediaStore.Audio.Media.DATA;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        // Check for database errors
        if (data == null || data.getCount() < 1) {
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
        folderAdapter.sort(new Comparator<Folder>() {
            @Override
            public int compare(Folder lhs, Folder rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (mCursor == null || mCursor.getCount() < 1) {
            return;
        }

        AdapterView.AdapterContextMenuInfo mi = (AdapterView.AdapterContextMenuInfo)menuInfo;
        mSelectedPosition = mi.position;
        mCursor.moveToFirst();
        String folderPath = folderAdapter.getItem(mSelectedPosition).getPath();
        String cursorPath = "";
        do {
            cursorPath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            cursorPath = cursorPath.substring(0, cursorPath.lastIndexOf("/"));
            if(folderPath.equals(cursorPath))
                break;
        }while(mCursor.moveToNext());

        menu.add(mFragmentGroupId, PLAY_SELECTION, 0, getResources().getString(R.string.play_all));
        String title = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        title = title.substring(0, title.lastIndexOf("/"));
        mCurrentId = title;
        title = title.substring(title.lastIndexOf("/")+1);;
        menu.setHeaderTitle(title);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId()==mFragmentGroupId){
            switch (item.getItemId()) {
                case PLAY_SELECTION:
                    long[] list = MusicUtils.getSongListForFolder(getActivity(), mCurrentId);
                    MusicUtils.playAll(getActivity(), list, 0);
                    break;
                default:
                    break;
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
