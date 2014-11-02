package com.energysistem.energyMusic.helpers.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;

/**
 * @author Vicente Giner Tendero
 * @version ${VERSION}
 */
public class FeedbackUtils {

    private Activity mActivity;

    public FeedbackUtils(Activity activity) {
        mActivity = activity;
    }

    public void sendFeedback() {
        mActivity.bindService(new Intent(Intent.ACTION_BUG_REPORT), new FeedbackServiceConnection(mActivity.getWindow()), Context.BIND_AUTO_CREATE);
    }

    protected static class FeedbackServiceConnection implements ServiceConnection {
        private static int MAX_WIDTH = 600;
        private static int MAX_HEIGHT = 600;

        protected final Window mWindow;

        public FeedbackServiceConnection(Window window) {
            this.mWindow = window;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Parcel parcel = Parcel.obtain();
                Bitmap bitmap = getScreenshot();
                if (bitmap != null) {
                    bitmap.writeToParcel(parcel, 0);
                }
                service.transact(IBinder.FIRST_CALL_TRANSACTION, parcel, null, 0);
                parcel.recycle();
            } catch (RemoteException e) {
                Log.e("ServiceConn", e.getMessage(), e);
            }
        }

        public void onServiceDisconnected(ComponentName name) { }

        private Bitmap getScreenshot() {
            try {
                View rootView = mWindow.getDecorView().getRootView();
                rootView.setDrawingCacheEnabled(true);
                Bitmap bitmap = rootView.getDrawingCache();
                if (bitmap != null)
                {
                    double height = bitmap.getHeight();
                    double width = bitmap.getWidth();
                    double ratio = Math.min(MAX_WIDTH / width, MAX_HEIGHT / height);
                    return Bitmap.createScaledBitmap(bitmap, (int)Math.round(width * ratio), (int)Math.round(height * ratio), true);
                }
            } catch (Exception e) {
                Log.e("Screenshoter", "Error getting current screenshot: ", e);
            }
            return null;
        }
    }
}
