/**
 *
 */

package com.energy.energyMusic.preferences;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.energy.energyMusic.IApolloService;
import com.energy.energyMusic.R;
import com.energy.energyMusic.cache.ImageProvider;
import com.energy.energyMusic.helpers.utils.ApolloUtils;
import com.energy.energyMusic.helpers.utils.MusicUtils;
import com.energy.energyMusic.service.ApolloService;
import com.energy.energyMusic.service.ServiceToken;

import static com.energy.energyMusic.Constants.WIDGET_STYLE;
import static com.energy.energyMusic.Constants.DELETE_CACHE;
import static com.energy.energyMusic.Constants.BUILD_VERSION;
import static com.energy.energyMusic.Constants.BUILD_DEPENDS;

@SuppressWarnings("deprecation")
public class SettingsHolder extends PreferenceActivity  implements ServiceConnection  {
    Context mContext;

    private ServiceToken mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This should be called first thing
        super.onCreate(savedInstanceState);
        mContext = this;
        // Load settings XML
        int preferencesResId = R.xml.settings;
        addPreferencesFromResource(preferencesResId);

        //Init widget style change option
        initChangeWidgetTheme();

        // Init delete cache option
        initDeleteCache();

        // Init about dialog
        initAboutDialog();

        initDependencies();

        initActionBar();
    }

    private void initActionBar() {
        ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


        LayoutInflater li = LayoutInflater.from(this);
        View customView=new View(this);
        if(!ApolloUtils.isTablet(this))
        {
            customView = li.inflate(R.layout.mibarrasmartphonevacia, null);
        }
        else
        {
            if(getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_MEDIUM && getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)
            {
                customView = li.inflate(R.layout.barra_tablet_mdpi_land, null);
            }
            else if(getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_MEDIUM && getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
            {
                customView = li.inflate(R.layout.barra_tablet_mdpi_port, null);
            }
            else if(getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_XHIGH && getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)
            {
                customView = li.inflate(R.layout.barra_tablet_xhdpi_land, null);
            }
            else if(getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_XHIGH && getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
            {
                customView = li.inflate(R.layout.barra_tablet_xhdpi_port, null);
            }
            else
            {
                //TODO:arreglar
                customView = li.inflate(R.layout.mibarrasmartphonevacia, null);
            }
        }

        bar.setCustomView(customView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initChangeWidgetTheme(){
        ListPreference listPreference = (ListPreference)findPreference(WIDGET_STYLE);
        listPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MusicUtils.notifyWidgets(ApolloService.META_CHANGED);
                return true;
            }
        });
    }

    private void initAboutDialog(){
        Preference aboutApolloMod = findPreference(BUILD_VERSION);
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(versionName!=null){
            try {
                aboutApolloMod.setSummary(versionName);
            }
            catch (Exception e){}
        }
        aboutApolloMod.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final TextView message = new TextView(mContext);
                message.setPadding(25, 25, 25, 25);
                // i.e.: R.string.dialog_message =>
                // "Test this dialog following the link to dtmilano.blogspot.com"
                final SpannableString s = new SpannableString(mContext.getText(R.string.about_apollomod_message));
                Linkify.addLinks(s, Linkify.WEB_URLS);
                message.setText(s);
                message.setMovementMethod(LinkMovementMethod.getInstance());

                new AlertDialog.Builder(SettingsHolder.this)
                        .setTitle(R.string.about_apollomod_title)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, null)
                        .setView(message)
                        .create()
                        .show();






                return true;
            }
        });

    }

    private void initDependencies(){
        final Preference buildDepend = findPreference(BUILD_DEPENDS);
        buildDepend.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                final WebView webView = new WebView(mContext);
                webView.loadUrl("file:///android_asset/licenses.html");
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.dependencies_title)
                        .setView(webView)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();
                return true;
            }
        });
    }
    /**
     * Removes all of the cache entries.
     */
    private void initDeleteCache() {
        final Preference deleteCache = findPreference(DELETE_CACHE);
        deleteCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                new AlertDialog.Builder(SettingsHolder.this).setMessage(R.string.delete_warning)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                ImageProvider.getInstance( (Activity) mContext ).clearAllCaches();
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                return true;
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder obj) {
        MusicUtils.mService = IApolloService.Stub.asInterface(obj);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        MusicUtils.mService = null;
    }

    @Override
    protected void onStart() {

        // Bind to Service
        mToken = MusicUtils.bindToService(this, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ApolloService.META_CHANGED);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Unbind
        if (MusicUtils.mService != null)
            MusicUtils.unbindFromService(mToken);

        //TODO: clear image cache

        super.onStop();
    }

}
