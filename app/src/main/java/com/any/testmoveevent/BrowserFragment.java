package com.any.testmoveevent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        final TextView pageTv = rootView.findViewById(R.id.pageTv);

        final int[] rid = {R.mipmap.log, R.mipmap.timg, R.mipmap.timt1, R.mipmap.timt2, R.mipmap.timt3};

        MyImageViewGoup imageViewGoup = rootView.findViewById(R.id.imageviewgroup);


        SubViewData2 data2 = new SubViewData2(rid,pageTv);
        imageViewGoup.setViewGoup(data2);

        return rootView;
    }

}
