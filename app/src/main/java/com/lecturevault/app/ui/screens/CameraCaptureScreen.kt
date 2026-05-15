package com.lecturevault.app.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.lecturevault.app.utils.FileUtils
import com.lecturevault.app.viewmodel.LectureViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(subjectId: Long, navController: NavController, vm: LectureViewModel = viewModel()) {
    val cameraPerm = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val executor = remember { Executors.newSingleThreadExecutor() }
    val captures = remember { mutableStateListOf<String>() }
    var saving by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("Lecture ${System.currentTimeMillis() % 10000}") }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    if (!cameraPerm.status.isGranted) {
        PermissionExplain("Camera access", "We need the camera to capture your notes.") {
            cameraPerm.launchPermissionRequest()
        }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Capture") }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) }
        }) },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${captures.size} pages")
                        FilledIconButton(
                            onClick = {
                                val file = FileUtils.newImageFile(context)
                                val output = ImageCapture.OutputFileOptions.Builder(file).build()
                                imageCapture?.takePicture(output, executor, object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(r: ImageCapture.OutputFileResults) {
                                        captures.add(file.absolutePath)
                                    }
                                    override fun onError(e: ImageCaptureException) {}
                                })
                            },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape
                        ) { Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(32.dp)) }
                        Button(
                            onClick = {
                                if (captures.isNotEmpty() && !saving) {
                                    saving = true
                                    scope.launch {
                                        vm.createNote(subjectId, null, title, "", captures.toList())
                                        navController.popBackStack()
                                    }
                                }
                            },
                            enabled = captures.isNotEmpty()
                        ) { Text("Save") }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { ctx ->
                val previewView = PreviewView(ctx)
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val ic = ImageCapture.Builder().build()
                    imageCapture = ic
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, ic)
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun PermissionExplain(title: String, message: String, onGrant: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onGrant) { Text("Grant permission") }
    }
}
