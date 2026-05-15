package com.lecturevault.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    val icon: String = "book",
    val semester: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
)

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val folderId: Long?,
    val title: String,
    val description: String? = null,
    val tags: String = "", // comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
)

@Entity(tableName = "note_pages")
data class NotePage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val imageUri: String,
    val pageOrder: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long?,
    val title: String,
    val reminderDateTime: Long,
    val isCompleted: Boolean = false,
)

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Subject>>
    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): Subject?
    @Insert suspend fun insert(subject: Subject): Long
    @Update suspend fun update(subject: Subject)
    @Delete suspend fun delete(subject: Subject)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE subjectId = :subjectId ORDER BY name")
    fun getBySubject(subjectId: Long): Flow<List<Folder>>
    @Insert suspend fun insert(folder: Folder): Long
    @Delete suspend fun delete(folder: Folder)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Note>>
    @Query("SELECT * FROM notes WHERE subjectId = :subjectId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getBySubject(subjectId: Long): Flow<List<Note>>
    @Query("SELECT * FROM notes WHERE isFavorite = 1 AND isDeleted = 0")
    fun getFavorites(): Flow<List<Note>>
    @Query("SELECT * FROM notes WHERE isDeleted = 1")
    fun getTrash(): Flow<List<Note>>
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' OR tags LIKE '%' || :q || '%') AND isDeleted = 0")
    fun search(q: String): Flow<List<Note>>
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?
    @Insert suspend fun insert(note: Note): Long
    @Update suspend fun update(note: Note)
    @Delete suspend fun delete(note: Note)
}

@Dao
interface NotePageDao {
    @Query("SELECT * FROM note_pages WHERE noteId = :noteId ORDER BY pageOrder")
    fun getByNote(noteId: Long): Flow<List<NotePage>>
    @Insert suspend fun insert(page: NotePage): Long
    @Update suspend fun update(page: NotePage)
    @Delete suspend fun delete(page: NotePage)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY reminderDateTime")
    fun getAll(): Flow<List<Reminder>>
    @Insert suspend fun insert(r: Reminder): Long
    @Update suspend fun update(r: Reminder)
    @Delete suspend fun delete(r: Reminder)
}

@Database(
    entities = [Subject::class, Folder::class, Note::class, NotePage::class, Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun notePageDao(): NotePageDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lecturevault.db"
                ).build().also { INSTANCE = it }
            }
    }
}
