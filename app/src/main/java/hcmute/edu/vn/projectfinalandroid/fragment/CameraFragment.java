package hcmute.edu.vn.projectfinalandroid.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.nl.translate.TranslateLanguage;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.controller.TextTranslator;

public class CameraFragment extends Fragment {
    private PreviewView previewView; // Nơi hiện màn hình camera
    private ImageCapture imageCapture; // Ảnh chụp để nhận diện văn bản
    private TextView tvOriginalText; // văn bản gốc
    private TextView tvTranslatedText; // văn bản dịch
    private TextTranslator textTranslator; // Dùng lớp TextTranslator để dịch văn bản
    private String sourceLang; // Ngôn ngữ gốc
    private String targetLang; // Ngôn ngữ dịch
    private String sourceLangName; // Tên của ngôn ngữ gốc
    private String targetLangName; // Tên của ngôn ngữ dịch
    private static final int CAMERA_PERMISSION_CODE = 100; // Biến kiểm tra quyền camera
    private static final int TARGET_IMAGE_SIZE = 800; // Kích thước tối đa sau khi resize hình chụp

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Ánh xạ UI
        previewView = view.findViewById(R.id.previewView);
        tvOriginalText = view.findViewById(R.id.tvOriginalText);
        tvTranslatedText = view.findViewById(R.id.tvTranslatedText);

        // Khởi tạo ngôn ngữ
        initializeLanguages(getArguments());

        // Khởi tạo TextTranslator
        setupTranslator();

        // Tạo event cho nút chụp
        view.findViewById(R.id.btnCapture).setOnClickListener(v -> takePhoto());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Kiểm tra quyền camera
        previewView.post(() -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraFragment", "Quyền camera được cấp");
            previewView.post(() -> startCamera());
        } else {
            Log.w("CameraFragment", "Quyền camera bị từ chối");
            Toast.makeText(requireContext(), "Cần cấp quyền camera", Toast.LENGTH_SHORT).show();
        }
    }

    // Khởi tạo ngôn ngữ
    private void initializeLanguages(Bundle args) {
        if (args != null && args.containsKey("sourceLang") && args.containsKey("targetLang")) {
            sourceLang = args.getString("sourceLang");
            targetLang = args.getString("targetLang");
            sourceLangName = args.getString("sourceLangName", "Unknown");
            targetLangName = args.getString("targetLangName", "Unknown");
            Log.d("CameraFragment", "Nhận ngôn ngữ từ Bundle: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        } else { // Nếu Bundle không có dữ liệu thì mặc định là dịch tiếng anh -> tiếng việt
            sourceLang = TranslateLanguage.ENGLISH;
            targetLang = TranslateLanguage.VIETNAMESE;
            sourceLangName = "English";
            targetLangName = "Vietnamese";
            Log.d("CameraFragment", "Không có Bundle hoặc thiếu dữ liệu, dùng mặc định: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        }
    }

    // Khởi tạo hàm dịch văn bản
    // Lấy hàm từ TextTranslator để dịch văn bản
    private void setupTranslator() {
        textTranslator = new TextTranslator(sourceLang, targetLang, new TextTranslator.TranslationCallback() {
            @Override
            public void onModelReady() {
                Log.d("CameraFragment", "Mô hình dịch sẵn sàng: " + sourceLangName + " -> " + targetLangName);
                Toast.makeText(requireContext(), "Mô hình dịch đã tải xong!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onModelDownloadFailed(String error) {
                Log.e("CameraFragment", "Lỗi tải mô hình dịch: " + error);
                Toast.makeText(requireContext(), "Lỗi tải mô hình dịch: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTranslationSuccess(String translatedText) {
                Log.d("CameraFragment", "Văn bản dịch: " + translatedText);
                tvTranslatedText.setText("Văn bản dịch: " + translatedText);
            }

            @Override
            public void onTranslationFailed(String errorMessage) {
                Log.e("CameraFragment", "Lỗi dịch: " + errorMessage);
                tvTranslatedText.setText("Văn bản dịch: Lỗi dịch văn bản");
                Toast.makeText(requireContext(), "Lỗi dịch: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sau khi đã có quyền thì mở camera lên
    private void startCamera() {
        Log.d("CameraFragment", "Bắt đầu khởi động camera");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d("CameraFragment", "CameraProvider đã sẵn sàng");

                Preview preview = new Preview.Builder()
                        .setTargetResolution(new Size(1920, 1080))
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(1920, 1080))
                        .setJpegQuality(100)
                        .build();

                if (previewView.getSurfaceProvider() == null) {
                    Log.e("CameraFragment", "SurfaceProvider của PreviewView là null");
                    Toast.makeText(requireContext(), "Lỗi: PreviewView chưa sẵn sàng", Toast.LENGTH_LONG).show();
                    return;
                }
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                try {
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
                    Log.d("CameraFragment", "Camera được gắn kết thành công");
                } catch (Exception e) {
                    Log.e("CameraFragment", "Không thể sử dụng camera sau, thử camera trước", e);
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
                    Log.d("CameraFragment", "Camera trước được gắn kết");
                }
            } catch (Exception e) {
                Log.e("CameraFragment", "Lỗi khởi động camera: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Lỗi khởi động camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // Xử lý nút chụp ảnh
    private void takePhoto() {
        Log.d("CameraFragment", "Chụp ảnh");
        File photoFile = new File(requireContext().getExternalFilesDir(null), "photo.jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Log.d("CameraFragment", "Ảnh đã được lưu: " + photoFile.getAbsolutePath());
                processImage(photoFile);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraFragment", "Lỗi khi chụp ảnh: " + exception.getMessage(), exception);
                Toast.makeText(requireContext(), "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Xử lý hình ảnh với Google Cloud Vision API
    private void processImage(File photoFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e("CameraFragment", "Không thể đọc ảnh thành Bitmap");
                Toast.makeText(requireContext(), "Lỗi đọc ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tiền xử lý ảnh
            Bitmap processedBitmap = preprocessImage(bitmap);
            if (processedBitmap == null) {
                Log.e("CameraFragment", "Lỗi tiền xử lý ảnh");
                Toast.makeText(requireContext(), "Lỗi tiền xử lý ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lấy góc xoay từ EXIF
            int rotationDegrees = getRotationDegrees(photoFile.getAbsolutePath());
            InputImage image = InputImage.fromBitmap(bitmap, rotationDegrees);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String recognizedText = text.getText();
                        if (!recognizedText.trim().isEmpty()) {
                            Log.d("CameraFragment", "Văn bản nhận diện: " + recognizedText);
                            tvOriginalText.setText("Văn bản gốc: " + recognizedText);
                            textTranslator.translateText(recognizedText, new TextTranslator.TranslationCallback() {
                                @Override
                                public void onModelReady() {
                                    // Không cần xử lý lại vì đã xử lý trong setupTranslator
                                }

                                @Override
                                public void onModelDownloadFailed(String error) {
                                    Log.e("CameraFragment", "Lỗi tải mô hình dịch: " + error);
                                    Toast.makeText(requireContext(), "Lỗi tải mô hình dịch: " + error, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onTranslationSuccess(String translatedText) {
                                    Log.d("CameraFragment", "Văn bản dịch: " + translatedText);
                                    tvTranslatedText.setText("Văn bản dịch: " + translatedText);
                                }

                                @Override
                                public void onTranslationFailed(String errorMessage) {
                                    Log.e("CameraFragment", "Lỗi dịch: " + errorMessage);
                                    tvTranslatedText.setText("Văn bản dịch: Lỗi dịch văn bản");
                                    Toast.makeText(requireContext(), "Lỗi dịch: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.d("CameraFragment", "Không tìm thấy văn bản trong ảnh");
                            tvOriginalText.setText("Văn bản gốc: Không tìm thấy văn bản");
                            tvTranslatedText.setText("Văn bản dịch: Không tìm thấy văn bản");
                            Toast.makeText(requireContext(), "Không tìm thấy văn bản", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CameraFragment", "Lỗi nhận diện: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Lỗi nhận diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("CameraFragment", "Lỗi xử lý ảnh: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Tiền xử lý ảnh (chuyển sang grayscale, resize, tăng độ tương phản)
    private Bitmap preprocessImage(Bitmap bitmap) {
        try {
            // Resize ảnh để giảm kích thước
            float scale = Math.min((float) TARGET_IMAGE_SIZE / bitmap.getWidth(), (float) TARGET_IMAGE_SIZE / bitmap.getHeight());
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.postScale(scale, scale);
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), scaleMatrix, true);

            // Chuyển sang grayscale
            Bitmap grayscaleBitmap = Bitmap.createBitmap(resizedBitmap.getWidth(), resizedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(grayscaleBitmap);
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0); // Chuyển sang grayscale
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(resizedBitmap, 0, 0, paint);

            // Tăng độ tương phản
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
            Log.e("CameraFragment", "Lỗi tiền xử lý ảnh: " + e.getMessage());
            return null;
        }
    }

    private int getRotationDegrees(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            Log.e("CameraFragment", "Lỗi đọc EXIF: " + e.getMessage());
            return 0;
        }
    }

    // Cập nhật ngôn ngữ khi thay đổi ngôn ngữ trên spinner của HomeActivity
    public void updateLanguages(Bundle bundle) {
        String newSourceLang = bundle.getString("sourceLang", "en");
        String newTargetLang = bundle.getString("targetLang", "vi");
        String newSourceLangName = bundle.getString("sourceLangName", "English");
        String newTargetLangName = bundle.getString("targetLangName", "Vietnamese");

        if (!newSourceLang.equals(sourceLang) || !newTargetLang.equals(targetLang)) {
            Log.d("CameraFragment", "Cập nhật ngôn ngữ từ " + sourceLangName + " -> " + targetLangName +
                    " thành " + newSourceLangName + " -> " + newTargetLangName);
            sourceLang = newSourceLang;
            targetLang = newTargetLang;
            sourceLangName = newSourceLangName;
            targetLangName = newTargetLangName;
            if (textTranslator != null) {
                textTranslator.close(); // Đóng translator cũ
            }
            setupTranslator(); // Khởi tạo translator mới
            tvTranslatedText.setText("Văn bản dịch: ");
            tvOriginalText.setText("Văn bản gốc: ");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("CameraFragment", "Đóng CameraFragment");
    }
}