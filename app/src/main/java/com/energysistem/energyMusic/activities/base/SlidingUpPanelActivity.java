package com.energysistem.energyMusic.activities.base;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.energysistem.energyMusic.R;
import com.energysistem.energyMusic.ui.fragments.BottomActionBarFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * @author Vicente Giner Tendero
 * 05/07/2014.
 */
public class SlidingUpPanelActivity extends ActionBarActivity {

    public final static String SAVED_INSTANCE_PANEL_STATE = "saved_instance_panel_state";
    protected SlidingUpPanelLayout mPanel;
    protected BottomActionBarFragment mBActionbar;
    protected boolean isAlreadyStarted = false;
    protected boolean isPanelExpanded = false;

    protected void onCreate(Bundle icicle) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(icicle);

        // Cargamos el estado anterior del SlidingUpPanelLayout
        // para luego configurarlo junto al BottomActionBarFragment
        if(icicle != null) {
            isPanelExpanded = icicle.getBoolean(SAVED_INSTANCE_PANEL_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Configuramos el BottomActionBarFragment
        mBActionbar =(BottomActionBarFragment) getSupportFragmentManager().findFragmentById(R.id.bottomactionbar_new);
        mBActionbar.setUpQueueSwitch(this);

        //Configuramos el SlidingUpPanelLayout
        mPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mPanel.setAnchorPoint(0);
        mPanel.setDragView(findViewById(R.id.bottom_action_bar_dragview));
        //TODO: revisar mPanel.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        mPanel.setAnchorPoint(0.0f);
        mPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                ActionBar actionBar = getSupportActionBar();
                if (slideOffset > 0.9) {
                    mBActionbar.onExpanded();
                    if (actionBar != null && actionBar.isShowing()) {
                        actionBar.hide();
                    }
                } else {
                    mBActionbar.onCollapsed();
                    if (actionBar != null && !actionBar.isShowing()) {
                        actionBar.show();
                    }
                }
            }
            @Override
            public void onPanelExpanded(View panel) {
                SlidingUpPanelActivity.this.onPanelExpanded(panel);
            }
            @Override
            public void onPanelCollapsed(View panel) {
                SlidingUpPanelActivity.this.onPanelCollapsed(panel);
            }
            @Override
            public void onPanelAnchored(View panel) {
            }
            @Override
            public void onPanelHidden(View view) {
            }
        });

        // Comprobamos el estado del SlidingUpPanelLayout
        if(isPanelExpanded) {
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                actionBar.hide();
            }
            mBActionbar.onExpanded();
        }

        // Comprobamos si la activity se ha lanzado desde un widget
        // para desplegar el SlidingUpPanelLayout
        String startedFrom = getIntent().getStringExtra("started_from");
        if(startedFrom!=null){
            ViewTreeObserver vto = mPanel.getViewTreeObserver();
            if(vto != null) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!isAlreadyStarted) {
                            //Esperamos a la activity
                            //TODO revisar
                            Thread expandThread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        sleep(700);
                                    } catch (InterruptedException e) {
                                        //
                                    }
                                    mPanel.expandPanel();
                                }
                            };
                            expandThread.start();
                            isAlreadyStarted = true;
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        // Guardamos el estado del SlidingUpPanelLayout
        icicle.putBoolean(SAVED_INSTANCE_PANEL_STATE, mPanel.isPanelExpanded());
    }

    @Override
    public void onBackPressed() {
        //Si el SlidingUpPanelLayout está abierto lo cerramos
        if(mPanel.isPanelExpanded()){
            mPanel.collapsePanel();
        }
        else{
            super.onBackPressed();
        }
    }

    protected void onPanelExpanded(View panel) {

    }

    protected void onPanelCollapsed(View panel) {

    }
}
