package com.any.testmoveevent;

import android.content.Context;
import android.graphics.Matrix;
import android.util.SparseArray;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import uk.co.senab.photoview.PhotoView;


/**
 * @author any
 * @date 2017/12/6
 */

public class SubViewData implements ISubView<PhotoView> {

    private List<String> lists;
    private TextView pageTv;

    public SubViewData(List<String> lists, TextView pageTv) {
        this.lists = lists;
        this.pageTv = pageTv;
    }

    @Override
    public SparseArray<PhotoView> getView(Context context) {
        SparseArray<PhotoView> views = new SparseArray<>(3);
        PhotoView photoView = new PhotoView(context);
        views.put(0, photoView);

        photoView = new PhotoView(context);
        views.put(1, photoView);

        photoView = new PhotoView(context);
        views.put(2, photoView);
        return views;
    }

    @Override
    public void destroyData(int page, PhotoView view) {
        Glide.clear(view);
    }

    @Override
    public void loadData(int page, PhotoView view) {
        Glide.with(view.getContext()).load(lists.get(page)).asBitmap().into(view);
    }

    @Override
    public void openPage(int page) {
        pageTv.setText((page + 1) + "/" + getSize());
    }

    @Override
    public int getSize() {
        return lists == null ? 0 : lists.size();
    }

}
