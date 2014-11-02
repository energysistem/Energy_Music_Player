package com.energysistem.energyMusic.ui.drawer;

/**
 * @author Vicente Giner Tendero
 * @version ${VERSION}
 */
public class DrawerMainItem extends DrawerItem {

    int mImageResourceId;

    public DrawerMainItem(String itemName, int imageResourceId) {
        super(itemName);
        this.mImageResourceId = imageResourceId;
    }

    public int getImageResourceId() {
        return mImageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.mImageResourceId = imageResourceId;
    }
}
