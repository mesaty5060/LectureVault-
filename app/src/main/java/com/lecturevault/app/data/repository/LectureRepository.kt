package com.lecturevault.app.data.repository

import android.content.Context
import com.lecturevault.app.data.database.*
import kotlinx.coroutines.flow.Flow

class LectureRepository(context: Context) {
    private val db = AppDatabase.get(context)
    private val subjectDao = db.subjectDao()
    private val folderDao = db.folderDao()
    private val noteDao = db.noteDao()
    private val pageDao = db.notePageDao()
    private val reminderDao = db.reminderDao()

    fun subjects(): Flow<List<Subject>> = subjectDao.getAll()
    suspend fun addSubject(s: Subject) = subjectDao.insert(s)
    suspend fun updateSubject(s: Subject) = subjectDao.update(s)
    suspend fun deleteSubject(s: Subject) = subjectDao.delete(s)
    suspend fun getSubject(id: Long) = subjectDao.getById(id)

    fun folders(subjectId: Long): Flow<List<Folder>> = folderDao.getBySubject(subjectId)
    suspend fun addFolder(f: Folder) = folderDao.insert(f)

    fun allNotes(): Flow<List<Note>> = noteDao.getAll()
    fun notesBySubject(id: Long): Flow<List<Note>> = noteDao.getBySubject(id)
    fun favorites(): Flow<List<Note>> = noteDao.getFavorites()
    fun trash(): Flow<List<Note>> = noteDao.getTrash()
    fun search(q: String): Flow<List<Note>> = noteDao.search(q)
    suspend fun getNote(id: Long) = noteDao.getById(id)
    suspend fun addNote(n: Note) = noteDao.insert(n)
    suspend fun updateNote(n: Note) = noteDao.update(n)
    suspend fun deleteNote(n: Note) = noteDao.delete(n)

    fun pages(noteId: Long): Flow<List<NotePage>> = pageDao.getByNote(noteId)
    suspend fun addPage(p: NotePage) = pageDao.insert(p)
    suspend fun deletePage(p: NotePage) = pageDao.delete(p)

    fun reminders(): Flow<List<Reminder>> = reminderDao.getAll()
    suspend fun addReminder(r: Reminder) = reminderDao.insert(r)
}
