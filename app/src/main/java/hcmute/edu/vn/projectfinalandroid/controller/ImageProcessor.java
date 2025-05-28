package hcmute.edu.vn.projectfinalandroid.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;

public class ImageProcessor {
    private static final String TAG = "ImageProcessor";
    private static final int TARGET_IMAGE_SIZE = 800; // Size ảnh sau khi chụp
    private final Context context;

    public ImageProcessor(Context context) {
        this.context = context;
    }

    // Tải bitmap và góc xoay từ nguồn (file hoặc URI)
    public Pair<Bitmap, Integer> loadBitmapAndRotation(Object source) throws IOException {
        Bitmap bitmap = null;
        int rotationDegrees = 0;
        ExifInterface exif = null;

        if (source instanceof String) { // From camera (file path)
            String filePath = (String) source;
            bitmap = BitmapFactory.decodeFile(filePath);
            try {
                exif = new ExifInterface(filePath);
            } catch (IOException e) {
                Log.e(TAG, "Error reading EXIF from file: " + e.getMessage());
            }
        } else if (source instanceof Uri) { // From storage (Uri)
            Uri imageUri = (Uri) source;
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                // Reopen stream for EXIF
                inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    try {
                        exif = new ExifInterface(inputStream);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading EXIF from URI: " + e.getMessage());
                    } finally {
                        inputStream.close();
                    }
                }
            }
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationDegrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationDegrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationDegrees = 270;
                    break;
                default:
                    rotationDegrees = 0;
                    break;
            }
        }

        return new Pair<>(bitmap, rotationDegrees);
    }

    // Xoay ảnh theo góc chỉ định
    public Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Tiền xử lý ảnh để nhận diện văn bản
    public Bitmap preprocessImage(Bitmap bitmap) {
        try {
            float scale = Math.min((float) TARGET_IMAGE_SIZE / bitmap.getWidth(), (float) TARGET_IMAGE_SIZE / bitmap.getHeight());
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.postScale(scale, scale);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), scaleMatrix, true);

            Bitmap grayscaleBitmap = Bitmap.createBitmap(resizedBitmap.getWidth(), resizedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(grayscaleBitmap);
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(resizedBitmap, 0, 0, paint);

            ColorMatrix contrastMatrix = new ColorMatrix();
            contrastMatrix.set(new float[] {
                    1.5f, 0, 0, 0, 0,
                    0, 1.5f, 0, 0, 0,
                    0, 0, 1.5f, 0, 0,
                    0, 0, 0, 1, 0
            });
            paint.setColorFilter(new ColorMatrixColorFilter(contrastMatrix));
            canvas.drawBitmap(grayscaleBitmap, 0, 0, paint);

            return grayscaleBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Image preprocessing error: " + e.getMessage());
            return null;
        }
    }
}