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

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.projectfinalandroid.R;

public class CameraFragment extends Fragment {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private TextView tvOriginalText;
    private TextView tvTranslatedText;
    private Translator translator;
    private String sourceLang;
    private String targetLang;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int TARGET_IMAGE_SIZE = 800; // Kích thước tối đa sau khi resize

    // Ánh xạ từ TranslateLanguage sang tên ngôn ngữ thân thiện
    private Map<String, String> codeToName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        previewView = view.findViewById(R.id.previewView);
        tvOriginalText = view.findViewById(R.id.tvOriginalText);
        tvTranslatedText = view.findViewById(R.id.tvTranslatedText);

        Bundle args = getArguments();
        if (args != null) {
            sourceLang = args.getString("sourceLang", TranslateLanguage.ENGLISH);
            targetLang = args.getString("targetLang", TranslateLanguage.VIETNAMESE);
            Log.d("CameraFragment", "Nhận ngôn ngữ từ Bundle: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        } else {
            sourceLang = TranslateLanguage.ENGLISH;
            targetLang = TranslateLanguage.VIETNAMESE;
            Log.d("CameraFragment", "Không có Bundle, dùng mặc định: sourceLang=" + sourceLang + ", targetLang=" + targetLang);
        }

        // Khởi tạo ánh xạ ngôn ngữ
        initializeCodeToName();

        // Khởi tạo trình phiên dịch
        setupTranslator();

        view.findViewById(R.id.btnCapture).setOnClickListener(v -> takePhoto());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView.post(() -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }
        });
    }

    // Khởi tạo ánh xạ từ TranslateLanguage sang tên ngôn ngữ thân thiện
    private void initializeCodeToName() {
        codeToName = new HashMap<>();
        codeToName.put(TranslateLanguage.AFRIKAANS, "Afrikaans");
        codeToName.put(TranslateLanguage.ALBANIAN, "Albanian");
        codeToName.put(TranslateLanguage.ARABIC, "Arabic");
        codeToName.put(TranslateLanguage.BELARUSIAN, "Belarusian");
        codeToName.put(TranslateLanguage.BENGALI, "Bengali");
        codeToName.put(TranslateLanguage.BULGARIAN, "Bulgarian");
        codeToName.put(TranslateLanguage.CATALAN, "Catalan");
        codeToName.put(TranslateLanguage.CHINESE, "Chinese");
        codeToName.put(TranslateLanguage.CROATIAN, "Croatian");
        codeToName.put(TranslateLanguage.CZECH, "Czech");
        codeToName.put(TranslateLanguage.DANISH, "Danish");
        codeToName.put(TranslateLanguage.DUTCH, "Dutch");
        codeToName.put(TranslateLanguage.ENGLISH, "English");
        codeToName.put(TranslateLanguage.ESTONIAN, "Estonian");
        codeToName.put(TranslateLanguage.FINNISH, "Finnish");
        codeToName.put(TranslateLanguage.FRENCH, "French");
        codeToName.put(TranslateLanguage.GALICIAN, "Galician");
        codeToName.put(TranslateLanguage.GERMAN, "German");
        codeToName.put(TranslateLanguage.GREEK, "Greek");
        codeToName.put(TranslateLanguage.HEBREW, "Hebrew");
        codeToName.put(TranslateLanguage.HINDI, "Hindi");
        codeToName.put(TranslateLanguage.HUNGARIAN, "Hungarian");
        codeToName.put(TranslateLanguage.ICELANDIC, "Icelandic");
        codeToName.put(TranslateLanguage.INDONESIAN, "Indonesian");
        codeToName.put(TranslateLanguage.ITALIAN, "Italian");
        codeToName.put(TranslateLanguage.JAPANESE, "Japanese");
        codeToName.put(TranslateLanguage.KOREAN, "Korean");
        codeToName.put(TranslateLanguage.LATVIAN, "Latvian");
        codeToName.put(TranslateLanguage.LITHUANIAN, "Lithuanian");
        codeToName.put(TranslateLanguage.MACEDONIAN, "Macedonian");
        codeToName.put(TranslateLanguage.MALAY, "Malay");
        codeToName.put(TranslateLanguage.NORWEGIAN, "Norwegian");
        codeToName.put(TranslateLanguage.PERSIAN, "Persian");
        codeToName.put(TranslateLanguage.POLISH, "Polish");
        codeToName.put(TranslateLanguage.PORTUGUESE, "Portuguese");
        codeToName.put(TranslateLanguage.ROMANIAN, "Romanian");
        codeToName.put(TranslateLanguage.RUSSIAN, "Russian");
        codeToName.put(TranslateLanguage.SLOVAK, "Slovak");
        codeToName.put(TranslateLanguage.SLOVENIAN, "Slovenian");
        codeToName.put(TranslateLanguage.SPANISH, "Spanish");
        codeToName.put(TranslateLanguage.SWAHILI, "Swahili");
        codeToName.put(TranslateLanguage.SWEDISH, "Swedish");
        codeToName.put(TranslateLanguage.THAI, "Thai");
        codeToName.put(TranslateLanguage.TURKISH, "Turkish");
        codeToName.put(TranslateLanguage.UKRAINIAN, "Ukrainian");
        codeToName.put(TranslateLanguage.VIETNAMESE, "Vietnamese");
        codeToName.put(TranslateLanguage.WELSH, "Welsh");
        codeToName.put(TranslateLanguage.ESPERANTO, "Esperanto");
        codeToName.put(TranslateLanguage.IRISH, "Irish");
        codeToName.put(TranslateLanguage.GUJARATI, "Gujarat");
        codeToName.put(TranslateLanguage.HAITIAN_CREOLE, "Creole Haiti");
        codeToName.put(TranslateLanguage.GEORGIAN, "Georgian");
        codeToName.put(TranslateLanguage.KANNADA, "Kannada");
        codeToName.put(TranslateLanguage.MARATHI, "Marathi");
        codeToName.put(TranslateLanguage.MALTESE, "Maltese");
        codeToName.put(TranslateLanguage.TAMIL, "Tamil");
        codeToName.put(TranslateLanguage.TELUGU, "Telugu");
        codeToName.put(TranslateLanguage.TAGALOG, "Tagalog");
        codeToName.put(TranslateLanguage.URDU, "Urdu");
    }

    // Khởi tạo trình phiên dịch
    private void setupTranslator() {
        if (translator != null) {
            translator.close();
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();
        translator = Translation.getClient(options);

        Toast.makeText(requireContext(), "Đang tải mô hình dịch...", Toast.LENGTH_SHORT).show();

        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> {
                    Log.d("CameraFragment", "Mô hình dịch sẵn sàng: " + codeToName.get(sourceLang) + " -> " + codeToName.get(targetLang));
                    Toast.makeText(requireContext(), "Mô hình dịch đã tải xong!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("CameraFragment", "Lỗi tải mô hình dịch: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Lỗi tải mô hình dịch: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Cập nhật ngôn ngữ từ spinner
    public void updateLanguages(Bundle bundle) {
        String newSourceLang = bundle.getString("sourceLang", TranslateLanguage.ENGLISH);
        String newTargetLang = bundle.getString("targetLang", TranslateLanguage.VIETNAMESE);

        if (!newSourceLang.equals(sourceLang) || !newTargetLang.equals(targetLang)) {
            Log.d("CameraFragment", "Cập nhật ngôn ngữ từ " + codeToName.get(sourceLang) + " -> " + codeToName.get(targetLang) +
                    " thành " + codeToName.get(newSourceLang) + " -> " + codeToName.get(newTargetLang));
            sourceLang = newSourceLang;
            targetLang = newTargetLang;
            setupTranslator();
            tvTranslatedText.setText("Văn bản dịch: ");
            tvOriginalText.setText("Văn bản gốc: ");
        } else {
            Log.d("CameraFragment", "Ngôn ngữ không thay đổi: " + codeToName.get(sourceLang) + " -> " + codeToName.get(targetLang));
        }
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

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String recognizedText = text.getText();
                        if (!recognizedText.trim().isEmpty()) {
                            Log.d("CameraFragment", "Văn bản nhận diện: " + recognizedText);
                            tvOriginalText.setText("Văn bản gốc: " + recognizedText);
                            translateText(recognizedText);
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

    private void translateText(String text) {
        Log.d("CameraFragment", "Dịch văn bản: " + text + " từ " + codeToName.get(sourceLang) + " sang " + codeToName.get(targetLang));
        translator.translate(text)
                .addOnSuccessListener(translatedText -> {
                    Log.d("CameraFragment", "Văn bản dịch: " + translatedText);
                    tvTranslatedText.setText("Văn bản dịch: " + translatedText);
                })
                .addOnFailureListener(e -> {
                    Log.e("CameraFragment", "Lỗi dịch: " + e.getMessage(), e);
                    tvTranslatedText.setText("Văn bản dịch: Lỗi dịch văn bản");
                    Toast.makeText(requireContext(), "Lỗi dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (translator != null) {
            translator.close();
        }
        Log.d("CameraFragment", "Đóng CameraFragment");
    }
}