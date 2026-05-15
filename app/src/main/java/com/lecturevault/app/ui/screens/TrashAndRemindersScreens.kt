package com.lecturevault.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lecturevault.app.viewmodel.LectureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(navController: NavController, vm: LectureViewModel = viewModel()) {
    val items by vm.trash.collectAsState()
    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Trash") },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
            actions = { if (items.isNotEmpty()) TextButton(onClick = { vm.emptyTrash() }) { Text("Empty") } }
        ) }
    ) { p ->
        if (items.isEmpty()) {
            Column(Modifier.padding(p)) { EmptyState("Trash is empty", "Deleted notes appear here", Icons.Default.Delete) }
        } else {
            LazyColumn(Modifier.padding(p)) {
                items(items) { n ->
                    ListItem(
                        headlineContent = { Text(n.title) },
                        leadingContent = { Icon(Icons.Default.Description, null) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { vm.restoreNote(n) }) { Icon(Icons.Default.Restore, null) }
                                IconButton(onClick = { vm.permanentlyDelete(n) }) { Icon(Icons.Default.DeleteForever, null) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(navController: NavController, vm: LectureViewModel = viewModel()) {
    val items by vm.reminders.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Reminders") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Add") }) }
    ) { p ->
        if (items.isEmpty()) {
            Column(Modifier.padding(p)) { EmptyState("No reminders", "Tap Add to create one", Icons.Default.Notifications) }
        } else {
            LazyColumn(Modifier.padding(p)) {
                items(items) { r ->
                    ListItem(
                        headlineContent = { Text(r.title) },
                        supportingContent = { Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(r.reminderDateTime))) },
                        leadingContent = { Icon(Icons.Default.Alarm, null) }
                    )
                }
            }
        }
    }
    if (showAdd) AddReminderDialog(onDismiss = { showAdd = false }) { title, millis ->
        vm.addReminder(null, title, millis); showAdd = false
    }
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var hoursFromNow by remember { mutableStateOf("1") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Reminder") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = hoursFromNow, onValueChange = { hoursFromNow = it.filter { c -> c.isDigit() } }, label = { Text("Hours from now") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val h = hoursFromNow.toLongOrNull() ?: 1L
                if (title.isNotBlank()) onConfirm(title, System.currentTimeMillis() + h * 3600_000L)
            }) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
