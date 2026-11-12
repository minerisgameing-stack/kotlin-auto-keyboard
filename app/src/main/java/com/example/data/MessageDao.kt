package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages")
    fun getMessageCount(): Flow<Int>

    @Query("SELECT * FROM messages ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomMessage(): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    @Query("UPDATE messages SET timesSent = timesSent + 1, lastSentAt = :timestamp WHERE id = :id")
    suspend fun incrementSentCount(id: Long, timestamp: Long)
}
