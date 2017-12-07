package com.any.testmoveevent;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

/**
 * @author any
 * @date 2017/12/6
 */

public interface ISubView<V extends View> {

    SparseArray<V> getView(Context context);

    void destroyData(int page,V view);

    void loadData(int page,V view);

    void openPage(int page);

    int getSize();
}
