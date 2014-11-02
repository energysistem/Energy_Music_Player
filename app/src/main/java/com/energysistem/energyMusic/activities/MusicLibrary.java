/**
 * 
 */

package com.energysistem.energyMusic.activities;

import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.energysistem.energyMusic.IApolloService;
import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.activities.base.SlidingUpPanelActivity;
import com.energysistem.energyMusic.helpers.utils.ApolloUtils;
import com.energysistem.energyMusic.helpers.utils.FeedbackUtils;
import com.energysistem.energyMusic.helpers.utils.MusicUtils;
import com.energysistem.energyMusic.helpers.utils.VisualizerUtils;
import com.energysistem.energyMusic.preferences.SettingsHolder;
import com.energysistem.energyMusic.service.ApolloService;
import com.energysistem.energyMusic.service.ServiceToken;
import com.energysistem.energyMusic.ui.adapters.PagerAdapter;
import com.energysistem.energyMusic.ui.adapters.ScrollingTabsAdapter;
import com.energysistem.energyMusic.ui.fragments.NavigationDrawerFragment;
import com.energysistem.energyMusic.ui.fragments.grid.AlbumsFragment;
import com.energysistem.energyMusic.ui.fragments.grid.ArtistsFragment;
import com.energysistem.energyMusic.ui.fragments.list.FolderFragment;
import com.energysistem.energyMusic.ui.fragments.list.GenresFragment;
import com.energysistem.energyMusic.ui.fragments.list.PlaylistsFragment;
import com.energysistem.energyMusic.ui.fragments.list.RecentlyAddedFragment;
import com.energysistem.energyMusic.ui.fragments.list.SongsFragment;
import com.energysistem.energyMusic.ui.widgets.ScrollableTabView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.energysistem.energyMusic.Constants.TABS_ENABLED;


/**
 * @author Andrew Neal
 * @Note This is the "holder" for all of the tabs
 */
public class MusicLibrary extends SlidingUpPanelActivity implements ServiceConnection,  NavigationDrawerFragment.NavigationDrawerCallbacks {

    private final static String RESTART_ACTIVITY = "com.energysistem.energyMusic.restartActivity";

    private ServiceToken mToken;
    protected Dialog mSplashDialog;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle icicle) {
        long tStart = 0;
        boolean isActivityRestarted = getIntent().getBooleanExtra(RESTART_ACTIVITY, false);

        if (!isActivityRestarted) {
            tStart = System.currentTimeMillis();
            showSplashScreen();
        }

        // Scan for music
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(icicle);

        super.setTheme(R.style.CustomActionBarTheme);

        VisualizerUtils.releaseVisualizer();

        // Landscape mode on phone isn't ready
        if (!ApolloUtils.isTablet(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Layout
        setContentView(R.layout.library_browser);

        // Style the actionbar
        initActionBar();

        initNavigationDrawer();

        // Control Media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Important!
        initPager();

        if (!isActivityRestarted) {
            long tEnd = System.currentTimeMillis();
            long elapsedTime = tEnd - tStart;
            removeSplashScreen(elapsedTime);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
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
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);

        Intent intent = getIntent();
        this.finish();
        startActivity(intent);
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

    /**
     * Initiate ViewPager and PagerAdapter
     */
    public void initPager() {
        // Initiate PagerAdapter
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

        //Get tab visibility preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> defaults = new HashSet<String>(Arrays.asList(
        		getResources().getStringArray(R.array.tab_titles_values)
        	));
        Set<String> tabs_set = sp.getStringSet(TABS_ENABLED,defaults);
        //if its empty fill reset it to full defaults
        	//stops app from crashing when no tabs are shown
        if(tabs_set.size()==0){
        	tabs_set = defaults;
        }

        //si veníamos de versión anterior, hay que coger los defaults

        //Only show tabs that were set in preferences
        // Recently added tracks
        int count;
        boolean bucle=true;
        do {
            count=0;

            if (tabs_set.contains(getResources().getString(R.string.tab_recent))) {
                mPagerAdapter.addFragment(new RecentlyAddedFragment());
                count++;
            }
            // Artists
            if (tabs_set.contains(getResources().getString(R.string.tab_artists))) {
                mPagerAdapter.addFragment(new ArtistsFragment());
                count++;
            }
            // Albums
            if (tabs_set.contains(getResources().getString(R.string.tab_albums))) {
                mPagerAdapter.addFragment(new AlbumsFragment());
                count++;
            }
            // // Tracks
            if (tabs_set.contains(getResources().getString(R.string.tab_songs))) {
                mPagerAdapter.addFragment(new SongsFragment());
                count++;
            }
            // // Folders
            if (tabs_set.contains(getResources().getString(R.string.tab_folders))) {
                mPagerAdapter.addFragment(new FolderFragment());
                count++;
            }
            // // Playlists
            if (tabs_set.contains(getResources().getString(R.string.tab_playlists))) {
                mPagerAdapter.addFragment(new PlaylistsFragment());
                count++;
            }
            // // Genres
            if (tabs_set.contains(getResources().getString(R.string.tab_genres))) {
                mPagerAdapter.addFragment(new GenresFragment());
                count++;
            }
            if(count!=tabs_set.size())
            {
                tabs_set=defaults;
                SharedPreferences.Editor editor=sp.edit();
                editor.putStringSet(TABS_ENABLED,defaults);
                editor.commit();
                mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
            }
            else
                bucle=false;
        }while(bucle);

        // Initiate ViewPager
        ViewPager mViewPager = (ViewPager)findViewById(R.id.viewPager);
        mViewPager.setPageMargin(getResources().getInteger(R.integer.viewpager_margin_width));
        mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mViewPager.setAdapter(mPagerAdapter);
        //mViewPager.setCurrentItem(0);

        // Tabs
        initScrollableTabs(mViewPager);

    }

    /**
     * Initiate the tabs
     */
    public void initScrollableTabs(ViewPager mViewPager) {
        ScrollableTabView mScrollingTabs = (ScrollableTabView)findViewById(R.id.scrollingTabs);
        ScrollingTabsAdapter mScrollingTabsAdapter = new ScrollingTabsAdapter(this);
        mScrollingTabs.setAdapter(mScrollingTabsAdapter);
        mScrollingTabs.setViewPager(mViewPager);
    }
    
    /**
     * For the theme chooser
     */
    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setLogo(R.drawable.stat_notify_music_alter);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    private void initNavigationDrawer() {
        mNavigationDrawerFragment =  (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);
    }
    
    /**
     * Respond to clicks on actionbar options
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.action_search:
	            onSearchRequested();
	            break;

	        case R.id.action_settings:
	        	startActivityForResult(new Intent(this, SettingsHolder.class),0);
	            break;

	        case R.id.action_eqalizer:
                final Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                if (getPackageManager().resolveActivity(intent, 0) == null) {
                    startActivity(new Intent(this, SimpleEq.class));
                }
                else{
                    //intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, (int)MusicUtils.getCurrentAudioId());
                    //startActivity(intent);
                    SharedPreferences prefs = getSharedPreferences("MisPreferencias",this.getApplicationContext().MODE_PRIVATE);
                    String audioid = prefs.getString("audioid", "0");
                    Intent i = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, Integer.valueOf(audioid));
                    i.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                    startActivityForResult(i, 0);
                }
                break;

	        case R.id.action_shuffle_all:
	        	shuffleAll();
	            break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Intent i = getBaseContext().getPackageManager()
	             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(RESTART_ACTIVITY, true);
		startActivity(i);
    }   

    /**
     * Initiate the Top Actionbar
     */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar_top, menu);
	    return true;
	}

	/**
     * Shuffle all the tracks
     */
    public void shuffleAll() {
        Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
            BaseColumns._ID
        };
        String selection = AudioColumns.IS_MUSIC + "=1";
        String sortOrder = "RANDOM()";
        Cursor cursor = MusicUtils.query(this, uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            MusicUtils.shuffleAll(this, cursor);
            cursor.close();
            cursor = null;
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        outState.putBoolean("SplashScreen", true);
    }

    /**
     * Removes the Dialog that displays the splash screen
     */
    protected void removeSplashScreen(long elapsedTime) {

        if (elapsedTime > 3000) {
            elapsedTime = 3000;
        }

        final long showTime = 3000 - elapsedTime;

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                        sleep(showTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mSplashDialog != null) {
                    mSplashDialog.dismiss();
                    mSplashDialog = null;
                }
            }
        };

        thread.start();
    }

    /**
     * Shows the splash screen over the full Activity
     */
    protected void showSplashScreen() {


        mSplashDialog = new Dialog(this, R.style.SplashScreen);
        mSplashDialog.setContentView(R.layout.splashscreen);
        mSplashDialog.setCancelable(false);
        mSplashDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mSplashDialog.show();

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 4:
                FeedbackUtils feedbackUtils = new FeedbackUtils(this);
                feedbackUtils.sendFeedback();
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(Gravity.START|Gravity.LEFT)){
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPanelExpanded(View panel) {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    protected void onPanelCollapsed(View panel) {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}
