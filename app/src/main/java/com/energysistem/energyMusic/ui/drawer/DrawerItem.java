package com.energysistem.energyMusic.ui.drawer;

/**
 * @author Vicente Giner Tendero
 * @version ${VERSION}
 */
public class DrawerItem {

    String mItemName;

    public DrawerItem(String itemName) {
        super();
        mItemName = itemName;
    }

    public String getItemName() {
        return mItemName;
    }
    public void setItemName(String itemName) {
        this.mItemName = itemName;
    }
}
