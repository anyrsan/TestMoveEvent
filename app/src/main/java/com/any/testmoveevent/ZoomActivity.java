package com.any.testmoveevent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * @author any
 * @date 2017/12/5
 */
public class ZoomActivity extends AppCompatActivity {

    private Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);


        findViewById(R.id.openImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImg(v, R.mipmap.log);
            }
        });

        mShortAnimationDuration = 2000;
    }


    private void zoomImg(View view, int rdImg) {

        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        //确认起点位置
        final Rect startBounds = new Rect();
        view.getGlobalVisibleRect(startBounds);

        final ImageView imgView = findViewById(R.id.expanded_image);
        imgView.setImageResource(rdImg);
        imgView.setVisibility(View.VISIBLE);


        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);


        Log.e("msg", "-->" + finalBounds + "==>" + globalOffset  +"==>" +getStatusBarHeight(this));

        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        imgView.setPivotX(0f);
        imgView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(imgView, View.X, startBounds.left,
                        finalBounds.left))
                .with(ObjectAnimator.ofFloat(imgView, View.Y, startBounds.top,
                        finalBounds.top))
                .with(ObjectAnimator.ofFloat(imgView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(imgView, View.SCALE_Y, startScale, 1f));

        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        final float startScaleFinal = startScale;
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel, back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(imgView, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(imgView, View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(imgView, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(imgView, View.SCALE_Y, startScaleFinal));

                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imgView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        imgView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });


    }




    // 获取状态栏高度，一般为 25dp
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}