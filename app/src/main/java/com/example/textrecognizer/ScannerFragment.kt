package com.example.textrecognizer

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        if (isGranted) startCamera() else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScannerBinding.bind(view)

        binding.tvRecognizedText.movementMethod = android.text.method.ScrollingMovementMethod()
        cameraPermissionRequest.launch(Manifest.permission.CAMERA)

        binding.btnScan.setOnClickListener { scanText() }

        binding.btnClear.setOnClickListener {
            binding.tvRecognizedText.text = getString(R.string.point_camera_text)
            Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show()
        }

        try {
            binding.root.findViewById<View>(R.id.btnCopy)?.setOnClickListener {
                val textToCopy = binding.tvRecognizedText.text.toString()
                if (textToCopy.isNotEmpty() && textToCopy != "Point camera at text...") {
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Scanned Text", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (_: Exception) { }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (_: Exception) { }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

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
                                val originalText = visionText.text
                                if (originalText.isEmpty()) {
                                    Toast.makeText(context, "No text found", Toast.LENGTH_SHORT).show()
                                } else {

                                    val emailRegex = Regex("[a-zA-Z0-9+._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,25}")

                                    val cardPattern = Regex("(\\d[\\s\\n]*){16}")

                                    val words = originalText.split("\\s+".toRegex())
                                    val censoredWords = words.map { word ->
                                        if (word.contains("@") || emailRegex.matches(word)) {
                                            "********@*******"
                                        } else {
                                            word
                                        }
                                    }

                                    var finalResult = censoredWords.joinToString(" ")

                                    finalResult = finalResult.replace(cardPattern) { matchResult ->
                                        matchResult.value.replace(Regex("\\d"), "*")
                                    }

                                    binding.tvRecognizedText.text = finalResult
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}