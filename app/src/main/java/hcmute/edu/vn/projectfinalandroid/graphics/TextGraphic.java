package hcmute.edu.vn.projectfinalandroid.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.mlkit.vision.text.Text;

public class TextGraphic extends GraphicOverlay.Graphic {
    private final Paint rectPaint;
    private final Text.TextBlock textBlock;
    private final OnTextClickListener listener;
    private boolean isSelected;

    public TextGraphic(GraphicOverlay overlay, Text.TextBlock textBlock, OnTextClickListener listener) {
        super(overlay);
        this.textBlock = textBlock;
        this.listener = listener;
        this.isSelected = false;

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.0f); // Đường viền
        rectPaint.setAntiAlias(true); // Làm mịn cạnh cho góc bo tròn
        updatePaintColor();

        overlay.postInvalidate();
    }

    private void updatePaintColor() {
        rectPaint.setColor(isSelected ? Color.RED : Color.YELLOW); // Đỏ khi chọn, vàng khi không chọn
    }

    @Override
    public void draw(Canvas canvas) {
        if (textBlock == null || textBlock.getBoundingBox() == null) {
            return;
        }

        RectF rect = new RectF(textBlock.getBoundingBox());
        // Mở rộng khung để lớn hơn một chút
        float inflateAmount = 5.0f;
        rect.left -= inflateAmount;
        rect.top -= inflateAmount;
        rect.right += inflateAmount;
        rect.bottom += inflateAmount;

        rect = overlay.translateRect(rect);
        updatePaintColor();
        canvas.drawRoundRect(rect, 10.0f, 10.0f, rectPaint); // Góc bo tròn với bán kính 16px
    }

    @Override
    public boolean contains(float x, float y) {
        if (textBlock == null || textBlock.getBoundingBox() == null) {
            return false;
        }
        RectF rect = new RectF(textBlock.getBoundingBox());
        float inflateAmount = 8.0f;
        rect.left -= inflateAmount;
        rect.top -= inflateAmount;
        rect.right += inflateAmount;
        rect.bottom += inflateAmount;
        rect = overlay.translateRect(rect);
        return rect.contains(x, y);
    }

    @Override
    public void onTap(float x, float y) {
        if (contains(x, y)) {
            isSelected = true; // Đánh dấu khung này được chọn
            // Bỏ chọn các khung khác
            for (GraphicOverlay.Graphic graphic : overlay.graphics) {
                if (graphic != this && graphic instanceof TextGraphic) {
                    ((TextGraphic) graphic).isSelected = false;
                }
            }
            listener.onTextClicked(textBlock.getText());
            overlay.postInvalidate(); // Vẽ lại để cập nhật màu
        }
    }

    public interface OnTextClickListener {
        void onTextClicked(String text);
    }
}
