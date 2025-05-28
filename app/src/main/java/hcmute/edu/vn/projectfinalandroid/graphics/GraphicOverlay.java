package hcmute.edu.vn.projectfinalandroid.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {
    private static final String TAG = "GraphicOverlay";
    final List<Graphic> graphics = new ArrayList<>();
    private int processedImageWidth;
    private int processedImageHeight;
    private int displayedImageWidth;
    private int displayedImageHeight;

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageDimensions(int processedWidth, int processedHeight, int displayedWidth, int displayedHeight) {
        this.processedImageWidth = processedWidth;
        this.processedImageHeight = processedHeight;
        this.displayedImageWidth = displayedWidth;
        this.displayedImageHeight = displayedHeight;
        Log.d(TAG, "setImageDimensions: processed=" + processedWidth + "x" + processedHeight +
                ", displayed=" + displayedWidth + "x" + displayedHeight);
        postInvalidate();
    }

    public void add(Graphic graphic) {
        graphics.add(graphic);
        postInvalidate();
    }

    public void clear() {
        graphics.clear();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Graphic graphic : graphics) {
            graphic.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            for (Graphic graphic : graphics) {
                if (graphic.contains(x, y)) {
                    graphic.onTap(x, y);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public RectF translateRect(RectF rect) {
        Log.d(TAG, "translateRect: processedWidth=" + processedImageWidth + ", processedHeight=" + processedImageHeight +
                ", viewWidth=" + getWidth() + ", viewHeight=" + getHeight());
        if (processedImageWidth == 0 || processedImageHeight == 0) {
            Log.w(TAG, "translateRect: Invalid processed image dimensions, returning original rect");
            return rect;
        }
        float scaleX = (float) getWidth() / processedImageWidth;
        float scaleY = (float) getHeight() / processedImageHeight;
        Log.d(TAG, "translateRect: scaleX=" + scaleX + ", scaleY=" + scaleY);
        return new RectF(
                rect.left * scaleX,
                rect.top * scaleY,
                rect.right * scaleX,
                rect.bottom * scaleY
        );
    }

    public abstract static class Graphic {
        protected GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public abstract boolean contains(float x, float y);

        public abstract void onTap(float x, float y);
    }
}