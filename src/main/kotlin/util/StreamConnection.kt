package net.hellz.util

import com.mongodb.client.model.ReplaceOptions
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.bson.Document

object StreamConnection {
    private val client: MongoClient = MongoClients.create("mongodb+srv://mcnaryy:RCbIk6g7kzDPNAed@hellz.ffcrq.mongodb.net/?retryWrites=true&w=majority&appName=HellZ")
    private val database: MongoDatabase = client.getDatabase("zNet")

    init {
        println("Successfully connected to the database.")
    }

    private fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }

    suspend fun writeAsync(collectionName: String, document: Document) {
        withContext(Dispatchers.IO) {
            try {
                getCollection(collectionName).insertOne(document).awaitSingle()
                println("[rStream] A document was inserted into the database.")
            } catch (e: Exception) {
                println("[rStream] Error inserting document: ${e.message}")
            }
        }
    }

    suspend fun readAsync(collectionName: String, filter: Document): Document? {
        return withContext(Dispatchers.IO) {
            try {
                getCollection(collectionName).find(filter).awaitFirstOrNull()
            } catch (e: Exception) {
                println("[rStream] Error reading document: ${e.message}")
                null
            }
        }
    }

    suspend fun updateAsync(collectionName: String, filter: Document, newDocument: Document) {
        withContext(Dispatchers.IO) {
            try {
                getCollection(collectionName).replaceOne(filter, newDocument, ReplaceOptions().upsert(true)).awaitSingle()
                println("[rStream] A document was successfully updated in the database!")
            } catch (e: Exception) {
                println("[rStream] Error updating document: ${e.message}")
            }
        }
    }
}