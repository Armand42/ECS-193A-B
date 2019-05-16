package com.google.cloud.android.speech;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {

    private Runnable scrollerTask;
    private int initialPosition;

    private int newCheck = 100;

    public interface OnScrollStoppedListener{
        void onScrollStopped();
    }

    private OnScrollStoppedListener onScrollStoppedListener;

    private IScrollListener listener = null;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        scrollerTask = new Runnable() {

            public void run() {

                int newPosition = getScrollY();
                if(initialPosition - newPosition == 0){//has stopped

                    if(onScrollStoppedListener!=null){

                        onScrollStoppedListener.onScrollStopped();
                    }
                }else{
                    initialPosition = getScrollY();
                    ObservableScrollView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };
    }

    public void setScrollViewListener(IScrollListener listener) {
        this.listener = listener;
    }

    public void setOnScrollStoppedListener(ObservableScrollView.OnScrollStoppedListener listener){
        onScrollStoppedListener = listener;
    }

    public void startScrollerTask(){
        initialPosition = getScrollY();
        ObservableScrollView.this.postDelayed(scrollerTask, newCheck);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (listener != null) {
            listener.onScrollChanged( this, x, y, oldx, oldy);
        }
    }
}
