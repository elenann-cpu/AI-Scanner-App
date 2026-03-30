package com.example.textrecognizer

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import com.example.textrecognizer.databinding.FragmentScannerBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScannerFragment : Fragment(R.layout.fragment_scanner) {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScannerBinding.bind(view)

        // Makes the text scrollable
        binding.tvRecognizedText.movementMethod = android.text.method.ScrollingMovementMethod()

        cameraPermissionRequest.launch(Manifest.permission.CAMERA)

        binding.btnScan.setOnClickListener {
            scanText()
        }

        // 4. ADD THIS NEW PART: The Clear Button
        binding.btnClear.setOnClickListener {
            binding.tvRecognizedText.text = "Point camera at text..."
            Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (_: Exception) {
                Toast.makeText(context, "Camera failed to start", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // THIS LINE FIXES THE RED ERROR (Line 73)
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun scanText() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                binding.tvRecognizedText.text = visionText.text
                                if (visionText.text.isEmpty()) {
                                    Toast.makeText(context, "No text found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            // THIS "_" FIXES THE YELLOW WARNING (Line 96)
                            .addOnFailureListener { _ ->
                                Toast.makeText(context, "Error: Scan failed", Toast.LENGTH_SHORT).show()
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}