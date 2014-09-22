
package com.energysistem.energyMusic.ui.adapters.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.helpers.Folder;
import com.energysistem.energyMusic.views.ViewHolderList;

import java.lang.ref.WeakReference;

public class FolderAdapter extends ArrayAdapter<Folder> {

    private WeakReference<ViewHolderList> holderReference;
    private LayoutInflater mLayoutInflater;
    private int mLayoutResourceId;
    private int left, top, right;

    public FolderAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        mLayoutResourceId = layoutResourceId;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        left = context.getResources().getDimensionPixelSize(
                R.dimen.listview_items_padding_left_top);
        top = context.getResources().getDimensionPixelSize(
                R.dimen.listview_items_padding_gp_top);
        right = context.getResources().getDimensionPixelSize(
                R.dimen.listview_items_padding_right);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View result = convertView;
        if (result == null) {
            result = mLayoutInflater.inflate(mLayoutResourceId, parent, false);
        }

        ImageView image = (ImageView) result.findViewById(R.id.listview_item_image);
        image.setImageResource(R.drawable.folder);
        image.setBackground(null);
        TextView lineOneTextView = (TextView) result.findViewById(R.id.listview_item_line_one);
        lineOneTextView.setText(getItem(position).getName());
        lineOneTextView.setPadding(left, top, right, 0);
        TextView lineTwoTextView = (TextView) result.findViewById(R.id.listview_item_line_two);
        lineTwoTextView.setVisibility(View.GONE);
        ImageView contextButton = (ImageView) result.findViewById(R.id.quick_context_tip);
        contextButton.setOnClickListener(showContextMenu);

        return result;
    }

    /**
     * Used to quickly show our the ContextMenu
     */
    private final View.OnClickListener showContextMenu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.showContextMenu();
        }
    };
}
