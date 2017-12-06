package com.any.testmoveevent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author any
 * @date 2017/12/5
 */

public class BrowserFragment extends Fragment {

    private BrowserImageViewGroup.IBrowserCloseView iBrowserCloseView;
    private BrowserImageViewGroup viewGroup;

    public void setIBrowserCloseView(BrowserImageViewGroup.IBrowserCloseView iBrowserCloseView) {
        this.iBrowserCloseView = iBrowserCloseView;
    }

    public void closeView(){
        viewGroup.closeView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_browser, container, false);

         viewGroup = rootView.findViewById(R.id.browserviewgroup);

        viewGroup.setIBrowserCloseView(iBrowserCloseView);


        ViewPager viewPager = rootView.findViewById(R.id.viewpager);

        final TextView pageTv = rootView.findViewById(R.id.pageTv);


        final int[] rid = {R.mipmap.log, R.mipmap.timg, R.mipmap.timt1, R.mipmap.timt2, R.mipmap.timt3};


        final List<View> views = new ArrayList<>();

        for (int i = 0; i < rid.length; i++) {
            ImageView view = (ImageView) getLayoutInflater().inflate(
                    R.layout.item_image, null);
            view.setImageResource(rid[i]);
            views.add(view);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pageTv.setText((position+1) + "/" + rid.length);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //初始化
        pageTv.setText(1 + "/" + rid.length);

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return rid.length;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(views.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = views.get(position);
                container.addView(view);
                return view;
            }
        });

        return rootView;
    }

}
