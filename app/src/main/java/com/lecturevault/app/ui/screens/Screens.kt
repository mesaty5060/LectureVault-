package com.lecturevault.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lecturevault.app.data.database.*
import com.lecturevault.app.utils.FileUtils
import com.lecturevault.app.utils.PdfExporter
import com.lecturevault.app.utils.ShareUtils
import com.lecturevault.app.viewmodel.LectureViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, vm: LectureViewModel = viewModel()) {
    val subjects by vm.subjects.collectAsState()
    val recent by vm.recentNotes.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("LectureVault", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val sid = subjects.firstOrNull()?.id
                    if (sid != null) navController.navigate("camera/$sid")
                },
                icon = { Icon(Icons.Default.PhotoCamera, null) },
                text = { Text("Capture") }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Text("Capture. Organize. Study Smarter.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = "", onValueChange = {},
                    placeholder = { Text("Search notes...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate("search") },
                    enabled = false
                )
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Subjects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null); Text("Add") }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (subjects.isEmpty()) {
                item { EmptyState("No subjects yet", "Tap Add to create your first subject", Icons.Default.Folder) }
            } else {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(subjects) { s -> SubjectCard(s) { navController.navigate("subject/${s.id}") } }
                    }
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                Text("Recent Captures", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
            }
            if (recent.isEmpty()) item { Text("Nothing here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else items(recent.take(8)) { n -> NoteRow(n, vm) { navController.navigate("note/${n.id}") } }
        }
    }

    if (showAdd) AddSubjectDialog(onDismiss = { showAdd = false }) { name, semester, color ->
        vm.addSubject(name, color, semester); showAdd = false
    }
}

@Composable
fun SubjectCard(s: Subject, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(s.color.toInt()).copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(Icons.Default.Book, null, tint = Color(s.color.toInt()))
            Spacer(Modifier.height(8.dp))
            Text(s.name, fontWeight = FontWeight.SemiBold)
            s.semester?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun NoteRow(n: Note, vm: LectureViewModel? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(n.title) },
        supportingContent = {
            Column {
                if (!n.description.isNullOrBlank()) Text(n.description)
                if (n.tags.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        n.tags.split(",").take(3).forEach { t ->
                            AssistChip(onClick = {}, label = { Text(t.trim(), style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                }
            }
        },
        leadingContent = { Icon(Icons.Default.Description, null) },
        trailingContent = {
            IconButton(onClick = { vm?.toggleFavorite(n) }) {
                Icon(if (n.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null,
                    tint = if (n.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun EmptyState(title: String, subtitle: String, icon: ImageVector, action: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null) { Spacer(Modifier.height(12.dp)); action() }
    }
}

@Composable
fun AddSubjectDialog(initial: Subject? = null, onDismiss: () -> Unit, onConfirm: (String, String?, Long) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var semester by remember { mutableStateOf(initial?.semester ?: "") }
    val colors = listOf(0xFF2563EBL, 0xFF7C3AEDL, 0xFFDC2626L, 0xFF059669L, 0xFFEA580CL, 0xFFDB2777L)
    var color by remember { mutableStateOf(initial?.color ?: colors.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Subject" else "Edit Subject") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = semester, onValueChange = { semester = it }, label = { Text("Semester (optional)") })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { c ->
                        Surface(
                            shape = RoundedCornerShape(50), color = Color(c.toInt()),
                            modifier = Modifier.size(32.dp).clickable { color = c },
                            border = if (c == color) BorderStroke(2.dp, Color.Black) else null
                        ) {}
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, semester.ifBlank { null }, color) }) { Text(if (initial == null) "Create" else "Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SubjectsScreen(navController: NavController, vm: LectureViewModel = viewModel()) = HomeScreen(navController, vm)

@Composable
fun FavoritesScreen(navController: NavController, vm: LectureViewModel = viewModel()) {
    val favs by vm.favorites.collectAsState()
    if (favs.isEmpty()) EmptyState("No favorites", "Star notes to find them here", Icons.Default.Star)
    else LazyColumn { items(favs) { n -> NoteRow(n, vm) { navController.navigate("note/${n.id}") } } }
}

@Composable
fun SearchScreen(navController: NavController, vm: LectureViewModel = viewModel()) {
    val q by vm.query.collectAsState()
    val results by vm.searchResults.collectAsState()
    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = q, onValueChange = vm::setQuery,
            placeholder = { Text("Search by title, tag, description") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )
        Spacer(Modifier.height(12.dp))
        if (results.isEmpty()) EmptyState("No results", "Try another keyword", Icons.Default.SearchOff)
        else LazyColumn { items(results) { n -> NoteRow(n, vm) { navController.navigate("note/${n.id}") } } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, vm: LectureViewModel = viewModel()) {
    val theme by vm.settings.theme.collectAsState(initial = "system")
    val quality by vm.settings.cameraQuality.collectAsState(initial = "high")
    val scope = rememberCoroutineScope()
    var themeDialog by remember { mutableStateOf(false) }
    var qualityDialog by remember { mutableStateOf(false) }

    LazyColumn(Modifier.padding(16.dp)) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }
        item {
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { Text(theme.replaceFirstChar { it.uppercase() }) },
                leadingContent = { Icon(Icons.Default.Palette, null) },
                modifier = Modifier.clickable { themeDialog = true }
            )
            ListItem(
                headlineContent = { Text("Camera quality") },
                supportingContent = { Text(quality.replaceFirstChar { it.uppercase() }) },
                leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                modifier = Modifier.clickable { qualityDialog = true }
            )
            ListItem(
                headlineContent = { Text("Reminders") },
                leadingContent = { Icon(Icons.Default.Alarm, null) },
                modifier = Modifier.clickable { navController.navigate("reminders") }
            )
            ListItem(
                headlineContent = { Text("Trash") },
                leadingContent = { Icon(Icons.Default.Delete, null) },
                modifier = Modifier.clickable { navController.navigate("trash") }
            )
            ListItem(headlineContent = { Text("Backup (coming soon)") }, leadingContent = { Icon(Icons.Default.CloudUpload, null) })
            ListItem(
                headlineContent = { Text("About LectureVault") },
                supportingContent = { Text("v1.0 · Capture. Organize. Study Smarter.") },
                leadingContent = { Icon(Icons.Default.Info, null) }
            )
        }
    }

    if (themeDialog) AlertDialog(
        onDismissRequest = { themeDialog = false },
        title = { Text("Theme") },
        text = {
            Column {
                listOf("system", "light", "dark").forEach { mode ->
                    Row(Modifier.fillMaxWidth().clickable { scope.launch { vm.settings.setTheme(mode); themeDialog = false } }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = theme == mode, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(mode.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {}
    )
    if (qualityDialog) AlertDialog(
        onDismissRequest = { qualityDialog = false },
        title = { Text("Camera quality") },
        text = {
            Column {
                listOf("high", "medium", "low").forEach { q ->
                    Row(Modifier.fillMaxWidth().clickable { scope.launch { vm.settings.setCameraQuality(q); qualityDialog = false } }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = quality == q, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(q.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(subjectId: Long, navController: NavController, vm: LectureViewModel = viewModel()) {
    val notes by vm.notesFor(subjectId).collectAsState(initial = emptyList())
    val folders by vm.foldersFor(subjectId).collectAsState(initial = emptyList())
    var subject by remember { mutableStateOf<Subject?>(null) }
    LaunchedEffect(subjectId) { subject = vm.getSubject(subjectId) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAddFolder by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val paths = uris.map { FileUtils.copyUriToInternal(context, it) }
                vm.createNote(subjectId, null, "Imported note", "", paths)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject?.name ?: "Subject") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; showEdit = true })
                        DropdownMenuItem(text = { Text("Archive") }, onClick = { menu = false; subject?.let { vm.archiveSubject(it); navController.popBackStack() } })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; subject?.let { vm.deleteSubject(it); navController.popBackStack() } })
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = { pickImages.launch("image/*") }) { Icon(Icons.Default.PhotoLibrary, null) }
                Spacer(Modifier.height(8.dp))
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("camera/$subjectId") },
                    icon = { Icon(Icons.Default.PhotoCamera, null) }, text = { Text("Capture") }
                )
            }
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).padding(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Folders", fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { showAddFolder = true }) { Icon(Icons.Default.Add, null); Text("New") }
                }
            }
            if (folders.isEmpty()) item { Text("No folders yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else items(folders) { f -> ListItem(headlineContent = { Text(f.name) }, leadingContent = { Icon(Icons.Default.Folder, null) }) }
            item { Spacer(Modifier.height(16.dp)); Text("Notes", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)) }
            if (notes.isEmpty()) item { EmptyState("No notes yet", "Capture or import to add notes", Icons.Default.Description) }
            else items(notes) { n -> NoteRow(n, vm) { navController.navigate("note/${n.id}") } }
        }
    }

    if (showAddFolder) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFolder = false },
            title = { Text("New Folder") },
            text = { OutlinedTextField(value = folderName, onValueChange = { folderName = it }, label = { Text("Folder name") }) },
            confirmButton = { TextButton(onClick = { if (folderName.isNotBlank()) { vm.addFolder(subjectId, folderName); showAddFolder = false } }) { Text("Create") } },
            dismissButton = { TextButton(onClick = { showAddFolder = false }) { Text("Cancel") } }
        )
    }

    if (showEdit && subject != null) AddSubjectDialog(initial = subject, onDismiss = { showEdit = false }) { name, sem, color ->
        vm.updateSubject(subject!!.copy(name = name, semester = sem, color = color))
        subject = subject!!.copy(name = name, semester = sem, color = color)
        showEdit = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewerScreen(noteId: Long, navController: NavController, vm: LectureViewModel = viewModel()) {
    val pages by vm.pagesFor(noteId).collectAsState(initial = emptyList())
    var note by remember { mutableStateOf<Note?>(null) }
    LaunchedEffect(noteId) { note = vm.getNote(noteId) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note?.title ?: "Note") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { note?.let { vm.toggleFavorite(it); note = it.copy(isFavorite = !it.isFavorite) } }) {
                        Icon(if (note?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder, null)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val uri = PdfExporter.exportNoteToPdf(context, note?.title ?: "note", pages.map { it.imageUri })
                            uri?.let {
                                val name = (note?.title ?: "note") + ".pdf"
                                val file = File(FileUtils.notesDir(context), name)
                                if (file.exists()) ShareUtils.shareFile(context, file)
                            }
                        }
                    }) { Icon(Icons.Default.PictureAsPdf, null) }
                    IconButton(onClick = { note?.let { vm.moveToTrash(it); navController.popBackStack() } }) { Icon(Icons.Default.Delete, null) }
                }
            )
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).padding(8.dp)) {
            items(pages) { page ->
                AsyncImage(model = page.imageUri, contentDescription = null, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text("${pages.size} page(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
