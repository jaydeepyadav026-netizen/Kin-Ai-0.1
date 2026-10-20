package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.Conversation
import com.example.data.model.MemoryCategory
import com.example.data.model.MessageRole
import com.example.data.model.NeuralMemory
import kotlinx.coroutines.flow.Flow

class Converters {
  @TypeConverter
  fun fromMessageRole(value: MessageRole): String = value.name

  @TypeConverter
  fun toMessageRole(value: String): MessageRole = try {
    MessageRole.valueOf(value)
  } catch (e: Exception) {
    MessageRole.USER
  }

  @TypeConverter
  fun fromChatMode(value: ChatMode): String = value.name

  @TypeConverter
  fun toChatMode(value: String): ChatMode = try {
    ChatMode.valueOf(value)
  } catch (e: Exception) {
    ChatMode.CHAT
  }

  @TypeConverter
  fun fromMemoryCategory(value: MemoryCategory): String = value.name

  @TypeConverter
  fun toMemoryCategory(value: String): MemoryCategory = try {
    MemoryCategory.valueOf(value)
  } catch (e: Exception) {
    MemoryCategory.PREFERENCE
  }
}

@Dao
interface ConversationDao {
  @Query("SELECT * FROM conversations ORDER BY lastUpdated DESC")
  fun getAllConversations(): Flow<List<Conversation>>

  @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
  suspend fun getConversationById(id: String): Conversation?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConversation(conversation: Conversation)

  @Update
  suspend fun updateConversation(conversation: Conversation)

  @Query("DELETE FROM conversations WHERE id = :id")
  suspend fun deleteConversation(id: String)

  @Query("DELETE FROM conversations")
  suspend fun clearAll()
}

@Dao
interface MessageDao {
  @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
  fun getMessagesForConversation(convId: String): Flow<List<ChatMessage>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessage)

  @Query("DELETE FROM messages WHERE conversationId = :convId")
  suspend fun deleteMessagesForConversation(convId: String)

  @Query("DELETE FROM messages")
  suspend fun clearAll()
}

@Dao
interface MemoryDao {
  @Query("SELECT * FROM neural_memories ORDER BY isPinned DESC, timestamp DESC")
  fun getAllMemories(): Flow<List<NeuralMemory>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: NeuralMemory)

  @Update
  suspend fun updateMemory(memory: NeuralMemory)

  @Delete
  suspend fun deleteMemory(memory: NeuralMemory)

  @Query("DELETE FROM neural_memories")
  suspend fun clearAll()
}

@Database(
  entities = [Conversation::class, ChatMessage::class, NeuralMemory::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KinAiDatabase : RoomDatabase() {
  abstract fun conversationDao(): ConversationDao
  abstract fun messageDao(): MessageDao
  abstract fun memoryDao(): MemoryDao

  companion object {
    @Volatile
    private var INSTANCE: KinAiDatabase? = null

    fun getInstance(context: Context): KinAiDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          KinAiDatabase::class.java,
          "kin_ai_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
