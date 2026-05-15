package com.lecturevault.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lecturevault.app.viewmodel.LectureViewModel
import kotlinx.coroutines.launch

private data class OnboardPage(val icon: ImageVector, val title: String, val body: String)
private val pages = listOf(
    OnboardPage(Icons.Default.PhotoCamera, "Capture instantly", "Snap any lecture page, whiteboard, or handout in one tap."),
    OnboardPage(Icons.Default.Folder, "Organize by subject", "Group notes by subject, semester, and folder."),
    OnboardPage(Icons.Default.PhotoLibrary, "No more gallery mess", "Study photos stay inside LectureVault, never in your phone gallery."),
    OnboardPage(Icons.Default.Search, "Find anything fast", "Search by title, tag, date, or description.")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit, vm: LectureViewModel = viewModel()) {
    val state = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    Scaffold { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            HorizontalPager(state = state, modifier = Modifier.weight(1f)) { page ->
                val p = pages[page]
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(p.icon, null, modifier = Modifier.size(96.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(24.dp))
                    Text(p.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(p.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    val selected = state.currentPage == i
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { scope.launch { vm.settings.setOnboarded(true); onDone() } }) { Text("Skip") }
                Button(onClick = {
                    if (state.currentPage < pages.lastIndex) scope.launch { state.animateScrollToPage(state.currentPage + 1) }
                    else scope.launch { vm.settings.setOnboarded(true); onDone() }
                }) { Text(if (state.currentPage == pages.lastIndex) "Get Started" else "Next") }
            }
        }
    }
}
