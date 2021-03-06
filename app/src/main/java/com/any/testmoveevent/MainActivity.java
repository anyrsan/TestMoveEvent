package com.any.testmoveevent;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * @author any
 * @date 2017/12/5
 */
public class MainActivity extends AppCompatActivity implements DragViewGroup.IBrowserCloseView {

    BrowserFragment browserFragment;
    public static final String FGTAG = "browse";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        browserFragment = new BrowserFragment();
        findViewById(R.id.openBrowser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Rect mid= new Rect();
                Point point = new Point();
                view.getGlobalVisibleRect(mid,point);

                getSupportFragmentManager().beginTransaction().replace(R.id.browserfg, browserFragment, FGTAG).commitAllowingStateLoss();
            }
        });
        browserFragment.setIBrowserCloseView(this);
    }


    @Override
    public void closeView() {
        getSupportFragmentManager().beginTransaction().remove(browserFragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        browserFragment = (BrowserFragment) getSupportFragmentManager().findFragmentByTag(FGTAG);
        if (browserFragment == null) {
            super.onBackPressed();
        } else {
            browserFragment.closeView();
        }

    }
}