package com.energysistem.energyMusic.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.ui.drawer.DrawerItem;
import com.energysistem.energyMusic.ui.drawer.DrawerMainItem;

import java.util.List;

/**
 * @author Vicente Giner Tendero
 * @version ${VERSION}
 */
public class DrawerAdapter extends ArrayAdapter<DrawerItem> {

    List<DrawerItem> mDrawerItemList;
    int mLayoutResourceId;

    public DrawerAdapter(Context context, int resource, List<DrawerItem> objects) {
        super(context, resource, objects);
        this.mDrawerItemList = objects;
        this.mLayoutResourceId = resource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DrawerMainItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            drawerHolder = new DrawerMainItemHolder();

            view = inflater.inflate(mLayoutResourceId, parent, false);
            //TODO
            //drawerHolder.ItemName = (TextView) view
            //        .findViewById(R.id.drawer_itemName);
            //drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerMainItemHolder) view.getTag();
        }

        DrawerItem dItem = (DrawerItem) this.mDrawerItemList.get(position);

        if(dItem instanceof DrawerMainItem) {
            drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(
                    ((DrawerMainItem) dItem).getImageResourceId()));
        }
        drawerHolder.ItemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerMainItemHolder {
        TextView ItemName;
        ImageView icon;
    }
}
