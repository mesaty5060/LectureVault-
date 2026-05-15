package com.lecturevault.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lecturevault.app.data.SettingsRepository
import com.lecturevault.app.data.database.*
import com.lecturevault.app.data.repository.LectureRepository
import com.lecturevault.app.utils.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LectureViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LectureRepository(app)
    val settings = SettingsRepository(app)

    val subjects = repo.subjects().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favorites = repo.favorites().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val recentNotes = repo.allNotes().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val trash = repo.trash().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val reminders = repo.reminders().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults = _query.flatMapLatest { q ->
        if (q.isBlank()) repo.allNotes() else repo.search(q)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setQuery(q: String) { _query.value = q }

    // Subjects
    fun addSubject(name: String, color: Long, semester: String?) = viewModelScope.launch {
        repo.addSubject(Subject(name = name, color = color, semester = semester))
    }
    fun updateSubject(s: Subject) = viewModelScope.launch { repo.updateSubject(s.copy(updatedAt = System.currentTimeMillis())) }
    fun deleteSubject(s: Subject) = viewModelScope.launch { repo.deleteSubject(s) }
    fun archiveSubject(s: Subject) = viewModelScope.launch { repo.updateSubject(s.copy(isArchived = true)) }
    suspend fun getSubject(id: Long) = repo.getSubject(id)

    // Folders
    fun foldersFor(subjectId: Long) = repo.folders(subjectId)
    fun addFolder(subjectId: Long, name: String) = viewModelScope.launch {
        repo.addFolder(Folder(subjectId = subjectId, name = name))
    }

    // Notes & pages
    fun notesFor(subjectId: Long) = repo.notesBySubject(subjectId)
    fun pagesFor(noteId: Long) = repo.pages(noteId)
    suspend fun getNote(id: Long) = repo.getNote(id)
    suspend fun createNote(subjectId: Long, folderId: Long?, title: String, tags: String, imagePaths: List<String>): Long {
        val noteId = repo.addNote(Note(subjectId = subjectId, folderId = folderId, title = title, tags = tags))
        imagePaths.forEachIndexed { i, p -> repo.addPage(NotePage(noteId = noteId, imageUri = p, pageOrder = i)) }
        return noteId
    }
    fun updateNote(n: Note) = viewModelScope.launch { repo.updateNote(n.copy(updatedAt = System.currentTimeMillis())) }
    fun toggleFavorite(n: Note) = viewModelScope.launch { repo.updateNote(n.copy(isFavorite = !n.isFavorite)) }
    fun moveToTrash(n: Note) = viewModelScope.launch { repo.updateNote(n.copy(isDeleted = true)) }
    fun restoreNote(n: Note) = viewModelScope.launch { repo.updateNote(n.copy(isDeleted = false)) }
    fun permanentlyDelete(n: Note) = viewModelScope.launch { repo.deleteNote(n) }
    fun emptyTrash() = viewModelScope.launch { trash.value.forEach { repo.deleteNote(it) } }

    // Reminders
    fun addReminder(noteId: Long?, title: String, dateTimeMillis: Long) = viewModelScope.launch {
        val id = repo.addReminder(Reminder(noteId = noteId, title = title, reminderDateTime = dateTimeMillis))
        ReminderScheduler.schedule(getApplication(), id.toInt(), title, dateTimeMillis)
    }
}
