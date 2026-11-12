package com.example.data

import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()
    val messageCount: Flow<Int> = messageDao.getMessageCount()

    suspend fun getRandomMessage(): MessageEntity? {
        return messageDao.getRandomMessage()
    }

    suspend fun insertMessage(text: String): Long {
        return messageDao.insert(MessageEntity(text = text.trim()))
    }

    suspend fun insertMessages(messages: List<String>) {
        val entities = messages.map { MessageEntity(text = it.trim()) }
        messageDao.insertAll(entities)
    }

    suspend fun updateMessage(id: Long, text: String) {
        val existing = messageDao.getMessageById(id)
        if (existing != null) {
            messageDao.update(existing.copy(text = text.trim()))
        }
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteById(id)
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAll()
    }

    suspend fun recordMessageSent(id: Long) {
        messageDao.incrementSentCount(id, System.currentTimeMillis())
    }
}
