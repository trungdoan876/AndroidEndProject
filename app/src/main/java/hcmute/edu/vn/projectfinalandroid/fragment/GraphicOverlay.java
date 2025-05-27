package hcmute.edu.vn.projectfinalandroid.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {
    private final List<Graphic> graphics = new ArrayList<>();

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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

    public abstract static class Graphic {
        private final GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public abstract boolean contains(float x, float y);

        public abstract void onTap(float x, float y);

        protected RectF translateRect(RectF rect) {
            float scaleX = (float) overlay.getWidth() / overlay.getMeasuredWidth();
            float scaleY = (float) overlay.getHeight() / overlay.getMeasuredHeight();
            return new RectF(
                    rect.left * scaleX,
                    rect.top * scaleY,
                    rect.right * scaleX,
                    rect.bottom * scaleY
            );
        }
    }
}