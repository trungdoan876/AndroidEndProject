package hcmute.edu.vn.projectfinalandroid.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.controller.TextTranslator;
import hcmute.edu.vn.projectfinalandroid.graphics.GraphicOverlay;
import hcmute.edu.vn.projectfinalandroid.graphics.TextGraphic;

public class CameraFragment extends Fragment {
    private PreviewView previewView; // Nơi hiện ống kính camera
    private ImageCapture imageCapture; // Hình sau khi chụp
    private ImageView capturedImageView; // View hiện hình sau khi chụp/chọn
    private GraphicOverlay graphicOverlay; // Dùng class GraphicOverlay để vẽ các khung bao quanh chữ
    private List<Text.TextBlock> textBlocks; // Các chữ để vẽ khung để bao quanh
    private TextView tvOriginalText; // txtView lưu văn bản gốc
    private TextView tvTranslatedText; // txtView lưu văn bản dịch
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior; // Nơi để hiện văn bản gốc & dịch
    private TextTranslator textTranslator; // Dùng class TextTranslator để dịch
    private String sourceLang; // Ngôn ngữ gốc
    private String targetLang; // Ngôn ngữ dịch
    private String sourceLangName; // Tên ngôn ngữ gốc
    private String targetLangName; // Tên ngôn ngữ dịch
    private ImageButton btnCapture; // Nút chụp hình
    private ImageButton btnUpload; // Nút tải ảnh lên
    private static final int CAMERA_PERMISSION_CODE = 16; // Biến để kiểm tra quyền camera
    private static final int STORAGE_PERMISSION_CODE = 101; // Biến để kiểm tra quyền truy cập kho lưu trữ
    private static final int PICK_IMAGE_REQUEST = 102; // Biến để kiểm tra quyền chọn hình
    private static final int TARGET_IMAGE_SIZE = 800; // Size ảnh sau khi chụp
    private static final String TAG = "CameraFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Initializing view");
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Ánh xạ UI
        previewView = view.findViewById(R.id.previewView);
        capturedImageView = view.findViewById(R.id.capturedImageView);
        graphicOverlay = view.findViewById(R.id.graphicOverlay);
        tvOriginalText = view.findViewById(R.id.tvOriginalText);
        tvTranslatedText = view.findViewById(R.id.tvTranslatedText);
        btnCapture = view.findViewById(R.id.btnCapture);
        btnUpload = view.findViewById(R.id.btnUpload);
        LinearLayout bottomSheet = view.findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        // Cho ẩn nơi dịch
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Khởi tạo ngôn ngữ (lấy ngôn ngữ gốc & ngôn ngữ dịch từ Bundle, từ HomeActivity truyền xuống)
        initializeLanguages(getArguments());
        // Khởi tạo hàm dịch ngôn ngữ
        setupTranslator();

        // Tạo event các nút bấm
        btnCapture.setOnClickListener(v -> takePhoto());
        btnUpload.setOnClickListener(v -> openImagePicker());

        // Tạo event kéo xuống cho khung hiện văn bản gốc & dịch
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            // Khi kéo xuống hết thì hiện camera lên
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    resetToCameraView();
                }
            }


            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Thêm hiệu ứng trượt nếu cần
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Checking camera permission");

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
        Log.d(TAG, "onRequestPermissionsResult: requestCode=" + requestCode);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                previewView.post(() -> startCamera());
            } else {
                Log.w(TAG, "Camera permission denied");
                Toast.makeText(requireContext(), "Camera permission required for this feature", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted");
                openImagePicker();
            } else {
                Log.w(TAG, "Storage permission denied");
                Toast.makeText(requireContext(), "Storage permission required to select images", Toast.LENGTH_LONG).show();
                if (!shouldShowRequestPermissionRationale(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                        Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(requireContext(), "Please grant storage permission in app settings", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Log.d(TAG, "Image URI: " + imageUri.toString());
            try {
                Pair<Bitmap, Integer> result = loadBitmapAndRotation(imageUri);
                Bitmap bitmap = result.first;
                int rotationDegrees = result.second;
                if (bitmap != null) {
                    if (rotationDegrees != 0) {
                        bitmap = rotateBitmap(bitmap, rotationDegrees);
                    }
                    processImage(bitmap);
                } else {
                    Log.e(TAG, "Bitmap is null");
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading image from storage: " + e.getMessage());
                Toast.makeText(requireContext(), "Error reading image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Image picking failed: requestCode=" + requestCode + ", resultCode=" + resultCode);
            Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show();
        }
    }

    private Pair<Bitmap, Integer> loadBitmapAndRotation(Object source) throws IOException {
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
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                // Reopen stream for EXIF
                inputStream = requireContext().getContentResolver().openInputStream(imageUri);
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

    private void initializeLanguages(Bundle args) {
        if (args != null && args.containsKey("sourceLang") && args.containsKey("targetLang")) {
            sourceLang = args.getString("sourceLang");
            targetLang = args.getString("targetLang");
            sourceLangName = args.getString("sourceLangName", "Unknown");
            targetLangName = args.getString("targetLangName", "Unknown");
            Log.d(TAG, "Received languages from Bundle: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        } else {
            sourceLang = TranslateLanguage.ENGLISH;
            targetLang = TranslateLanguage.VIETNAMESE;
            sourceLangName = "English";
            targetLangName = "Vietnamese";
            Log.d(TAG, "No Bundle or missing data, using default: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        }
    }

    private void setupTranslator() {
        textTranslator = new TextTranslator(sourceLang, targetLang, new TextTranslator.TranslationCallback() {
            @Override
            public void onModelReady() {
                Log.d(TAG, "Translation model ready: " + sourceLangName + " -> " + targetLangName);
                Toast.makeText(requireContext(), "Translation model loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onModelDownloadFailed(String error) {
                Log.e(TAG, "Translation model download failed: " + error);
                Toast.makeText(requireContext(), "Translation model download error: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTranslationSuccess(String translatedText) {
                Log.d(TAG, "Translated text: " + translatedText);
                tvTranslatedText.setText("Translated text: " + translatedText);
            }

            @Override
            public void onTranslationFailed(String errorMessage) {
                Log.e(TAG, "Translation error: " + errorMessage);
                tvTranslatedText.setText("Translated text: Translation error");
                Toast.makeText(requireContext(), "Translation error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCamera() {
        Log.d(TAG, "Starting camera");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "CameraProvider ready");

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setJpegQuality(100)
                        .build();

                if (previewView.getSurfaceProvider() == null) {
                    Log.e(TAG, "PreviewView SurfaceProvider is null");
                    Toast.makeText(requireContext(), "Error: PreviewView not ready", Toast.LENGTH_LONG).show();
                    return;
                }
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                try {
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
                    Log.d(TAG, "Camera bound successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to use back camera, trying front camera", e);
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
                    Log.d(TAG, "Front camera bound");
                }
            } catch (Exception e) {
                Log.e(TAG, "Camera start error: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Camera start error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        Log.d(TAG, "Capturing photo");
        File photoFile = new File(requireContext().getExternalFilesDir(null), "photo.jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Log.d(TAG, "Photo saved: " + photoFile.getAbsolutePath());
                try {
                    Pair<Bitmap, Integer> result = loadBitmapAndRotation(photoFile.getAbsolutePath());
                    Bitmap bitmap = result.first;
                    int rotationDegrees = result.second;
                    if (bitmap != null) {
                        if (rotationDegrees != 0) {
                            bitmap = rotateBitmap(bitmap, rotationDegrees);
                        }
                        Log.d(TAG, "Captured image resolution: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        processImage(bitmap);
                    } else {
                        Log.e(TAG, "Bitmap is null");
                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading image from file: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error reading image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture error: " + exception.getMessage(), exception);
                Toast.makeText(requireContext(), "Photo capture error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void openImagePicker() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
        } else {
            requestPermissions(new String[]{permission}, STORAGE_PERMISSION_CODE);
        }
    }

    private void processImage(Bitmap bitmap) {
        try {
            if (bitmap == null) {
                Log.e(TAG, "Failed to read image as Bitmap");
                Toast.makeText(requireContext(), "Error reading image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap processedBitmap = preprocessImage(bitmap);
            if (processedBitmap == null) {
                Log.e(TAG, "Image preprocessing error");
                Toast.makeText(requireContext(), "Image preprocessing error", Toast.LENGTH_SHORT).show();
                return;
            }

            previewView.setVisibility(View.GONE);
            capturedImageView.setImageBitmap(bitmap);
            capturedImageView.setVisibility(View.VISIBLE);
            graphicOverlay.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            btnCapture.setVisibility(View.GONE);
            btnUpload.setVisibility(View.GONE);

            // Truyền kích thước thực tế của capturedImageView
            graphicOverlay.setImageDimensions(
                    processedBitmap.getWidth(),
                    processedBitmap.getHeight(),
                    capturedImageView.getWidth(),
                    capturedImageView.getHeight()
            );

            InputImage image = InputImage.fromBitmap(processedBitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        textBlocks = text.getTextBlocks();
                        if (!textBlocks.isEmpty()) {
                            graphicOverlay.clear();
                            for (Text.TextBlock block : textBlocks) {
                                graphicOverlay.add(new TextGraphic(graphicOverlay, block, new TextGraphic.OnTextClickListener() {
                                    @Override
                                    public void onTextClicked(String text1) {
                                        tvOriginalText.setText("Original text: " + text1);
                                        textTranslator.translateText(text1, new TextTranslator.TranslationCallback() {
                                            @Override
                                            public void onModelReady() {}
                                            @Override
                                            public void onModelDownloadFailed(String error) {
                                                Log.e(TAG, "Translation model download error: " + error);
                                                Toast.makeText(requireContext(), "Translation model download error: " + error, Toast.LENGTH_LONG).show();
                                            }
                                            @Override
                                            public void onTranslationSuccess(String translatedText) {
                                                Log.d(TAG, "Translated text: " + translatedText);
                                                tvTranslatedText.setText("Translated text: " + translatedText);
                                            }
                                            @Override
                                            public void onTranslationFailed(String errorMessage) {
                                                Log.e(TAG, "Translation error: " + errorMessage);
                                                tvTranslatedText.setText("Translated text: Translation error");
                                                Toast.makeText(requireContext(), "Translation error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }));
                            }
                            String firstText = textBlocks.get(0).getText();
                            tvOriginalText.setText("Original text: " + firstText);
                            textTranslator.translateText(firstText, new TextTranslator.TranslationCallback() {
                                @Override
                                public void onModelReady() {}
                                @Override
                                public void onModelDownloadFailed(String error) {
                                    Log.e(TAG, "Translation model download error: " + error);
                                    Toast.makeText(requireContext(), "Translation model download error: " + error, Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onTranslationSuccess(String translatedText) {
                                    Log.d(TAG, "Translated text: " + translatedText);
                                    tvTranslatedText.setText("Translated text: " + translatedText);
                                }
                                @Override
                                public void onTranslationFailed(String errorMessage) {
                                    Log.e(TAG, "Translation error: " + errorMessage);
                                    tvTranslatedText.setText("Translated text: Translation error");
                                    Toast.makeText(requireContext(), "Translation error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.d(TAG, "No text found in image");
                            tvOriginalText.setText("Original text: No text found");
                            tvTranslatedText.setText("Translated text: No text found");
                            Toast.makeText(requireContext(), "No text found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Text recognition error: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Text recognition error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Image processing error: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Image processing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap preprocessImage(Bitmap bitmap) {
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

    public void updateLanguages(Bundle bundle) {
        String newSourceLang = bundle.getString("sourceLang", "en");
        String newTargetLang = bundle.getString("targetLang", "vi");
        String newSourceLangName = bundle.getString("sourceLangName", "English");
        String newTargetLangName = bundle.getString("targetLangName", "Vietnamese");

        if (!newSourceLang.equals(sourceLang) || !newTargetLang.equals(targetLang)) {
            Log.d(TAG, "Updating languages from " + sourceLangName + " -> " + targetLangName +
                    " to " + newSourceLangName + " -> " + newTargetLangName);
            sourceLang = newSourceLang;
            targetLang = newTargetLang;
            sourceLangName = newSourceLangName;
            targetLangName = newTargetLangName;
            if (textTranslator != null) {
                textTranslator.close();
            }
            setupTranslator();
            tvTranslatedText.setText("Translated text: ");
            tvOriginalText.setText("Original text: ");
        }
    }

    private void resetToCameraView() {
        Log.d(TAG, "resetToCameraView: Resetting to camera view");
        previewView.setVisibility(View.VISIBLE);
        capturedImageView.setVisibility(View.GONE);
        graphicOverlay.setVisibility(View.GONE);
        btnCapture.setVisibility(View.VISIBLE);
        btnUpload.setVisibility(View.VISIBLE);
        tvOriginalText.setText("Original text: No text");
        tvTranslatedText.setText("Translated text: No text");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        startCamera();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Closing CameraFragment");
    }
}