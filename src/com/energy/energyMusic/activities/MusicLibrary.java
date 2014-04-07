/**
 * 
 */

package com.energy.energyMusic.activities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
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
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.energy.energyMusic.IApolloService;
import com.energy.energyMusic.R;
import com.energy.energyMusic.helpers.utils.VisualizerUtils;
import com.energy.energyMusic.ui.adapters.PagerAdapter;
import com.energy.energyMusic.ui.fragments.BottomActionBarFragment;
import com.energy.energyMusic.ui.adapters.ScrollingTabsAdapter;
import com.energy.energyMusic.ui.fragments.grid.AlbumsFragment;
import com.energy.energyMusic.ui.fragments.grid.ArtistsFragment;
import com.energy.energyMusic.ui.fragments.list.GenresFragment;
import com.energy.energyMusic.ui.fragments.list.PlaylistsFragment;
import com.energy.energyMusic.ui.fragments.list.RecentlyAddedFragment;
import com.energy.energyMusic.ui.fragments.list.SongsFragment;
import com.energy.energyMusic.helpers.utils.ApolloUtils;
import com.energy.energyMusic.helpers.utils.MusicUtils;
import com.energy.energyMusic.preferences.SettingsHolder;
import com.energy.energyMusic.service.ApolloService;
import com.energy.energyMusic.service.ServiceToken;
import com.energy.energyMusic.ui.widgets.ScrollableTabView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import static com.energy.energyMusic.Constants.TABS_ENABLED;


/**
 * @author Andrew Neal
 * @Note This is the "holder" for all of the tabs
 */
public class MusicLibrary extends FragmentActivity implements ServiceConnection {

    private SlidingUpPanelLayout mPanel;

    private ServiceToken mToken;

    public static final String SAVED_STATE_ACTION_BAR_HIDDEN = "saved_state_action_bar_hidden";

    BottomActionBarFragment mBActionbar;

    private boolean isAlreadyStarted = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        VisualizerUtils.releaseVisualizer();

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        // Landscape mode on phone isn't ready
        if (!ApolloUtils.isTablet(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Scan for music
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Layout
        setContentView(R.layout.library_browser);

        mBActionbar =(BottomActionBarFragment) getSupportFragmentManager().findFragmentById(R.id.bottomactionbar_new);

        mBActionbar.setUpQueueSwitch(this);

        mPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        mPanel.setAnchorPoint(0);

        mPanel.setDragView(findViewById(R.id.bottom_action_bar_dragview));
        mPanel.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        mPanel.setAnchorPoint(0.0f);
        mPanel.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.2) {
                    mBActionbar.onExpanded();
                    if (getActionBar().isShowing()) {
                        getActionBar().hide();
                    }
                } else {
                    mBActionbar.onCollapsed();
                    if (!getActionBar().isShowing()) {
                        getActionBar().show();
                    }
                }
            }
            @Override
            public void onPanelExpanded(View panel) {
            }
            @Override
            public void onPanelCollapsed(View panel) {
            }
            @Override
            public void onPanelAnchored(View panel) {
            }
        });

        String startedFrom = getIntent().getStringExtra("started_from");
        if(startedFrom!=null){
            ViewTreeObserver vto = mPanel.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if(!isAlreadyStarted){
                        mPanel.expandPane();
                        isAlreadyStarted=true;
                    }
                }
            });
        }

        // Style the actionbar
        initActionBar();

        // Control Media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Important!
        initPager();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(mPanel.isExpanded()){
            mPanel.collapsePane();
        }
        else{
            super.onBackPressed();
        }
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

    /**
     * Initiate ViewPager and PagerAdapter
     */
    public void initPager() {
        // Initiate PagerAdapter
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

        //Get tab visibility preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> defaults = new HashSet<String>(Arrays.asList(
        		getResources().getStringArray(R.array.tab_titles)
        	));
        Set<String> tabs_set = sp.getStringSet(TABS_ENABLED,defaults);
        //if its empty fill reset it to full defaults
        	//stops app from crashing when no tabs are shown
        if(tabs_set.size()==0){
        	tabs_set = defaults;
        }
        
        //Only show tabs that were set in preferences
        // Recently added tracks
        if(tabs_set.contains(getResources().getString(R.string.tab_recent)))
        	mPagerAdapter.addFragment(new RecentlyAddedFragment());
        // Artists
        if(tabs_set.contains(getResources().getString(R.string.tab_artists)))
        	mPagerAdapter.addFragment(new ArtistsFragment());
        // Albums
        if(tabs_set.contains(getResources().getString(R.string.tab_albums)))
        	mPagerAdapter.addFragment(new AlbumsFragment());
        // // Tracks
        if(tabs_set.contains(getResources().getString(R.string.tab_songs)))
        	mPagerAdapter.addFragment(new SongsFragment());
        // // Playlists
        if(tabs_set.contains(getResources().getString(R.string.tab_playlists)))
        	mPagerAdapter.addFragment(new PlaylistsFragment());
        // // Genres
        if(tabs_set.contains(getResources().getString(R.string.tab_genres)))
        	mPagerAdapter.addFragment(new GenresFragment());

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
/*
    	ActionBar actBar = getActionBar();
        
        // The ActionBar Title and UP ids are hidden.
        int upId = Resources.getSystem().getIdentifier("up", "id", "android");
        
        ImageView actionBarUp = (ImageView)findViewById(upId);

        // Theme chooser
        ThemeUtils.setActionBarBackground(this, actBar, "action_bar_background");
        ThemeUtils.initThemeChooser(this, actionBarUp, "action_bar_up", THEME_ITEM_BACKGROUND);

    	actBar.setDisplayUseLogoEnabled(true);
        actBar.setDisplayShowTitleEnabled(false);


*/
        ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


        LayoutInflater li = LayoutInflater.from(this);
        View customView=new View(this);
        if(!ApolloUtils.isTablet(this))
        {
            if(ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey())
            {
                customView = li.inflate(R.layout.mibarrasmartphone_solo_busqueda, null);
            }
            else
            {
                customView = li.inflate(R.layout.mibarrasmartphone, null);
            }
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
                customView=li.inflate(R.layout.mi_barra_alternativa,null);
            }
        }
        bar.setCustomView(customView);
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
                //TODO: revisar cambio a original
    /*
                SharedPreferences prefs = getSharedPreferences("MisPreferencias",this.getApplicationContext().MODE_PRIVATE);
                String audioid = prefs.getString("audioid", "0");
                //Log.d("audioid_cheto",audioid);
	        	Intent i = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, Integer.valueOf(audioid));
                i.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                startActivityForResult(i, 0);
                */
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

}
