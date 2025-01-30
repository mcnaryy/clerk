package net.hellz.util

import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.Document
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import javax.print.Doc

class StreamConnection {
    private val client: MongoClient = MongoClients.create("mongodb+srv://mcnaryy:RCbIk6g7kzDPNAed@hellz.ffcrq.mongodb.net/?retryWrites=true&w=majority&appName=HellZ")
    private val database: MongoDatabase = client.getDatabase("zNet")

    init {
        println("Successfully connected to the database.")
    }

    // Retrieves the specified collection from the database
    fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }

    // Write to the specified collection using a document
    fun write(collectionName: String, document: Document){
        getCollection(collectionName).insertOne(document).subscribe(object : Subscriber<InsertOneResult> {
            override fun onSubscribe(subscription: Subscription) {
                subscription.request(1)
            }

            override fun onNext(result: InsertOneResult){
                println("[rStream] A document was inserted into the database.")
            }

            // Error handling
            override fun onError(t: Throwable?) {
                println("[rStream] There was an error inserting a document to the database.")
            }

            override fun onComplete() {}
        })
    }

    // Reads a specified collection for a document
    fun read(collectionName: String, filter: Document, callback: (Document?) -> Unit) {
        getCollection(collectionName).find(filter).first().subscribe(object : Subscriber<Document> {
            override fun onSubscribe(subscription: Subscription) {
                subscription.request(1)
            }

            override fun onNext(result: Document) {
                callback(result)
            }

            override fun onError(t: Throwable) {
                println("[rStream] Error reading document: ${t.message}")
            }

            override fun onComplete() {}
        })
    }

    // Updates a document from the collection
    fun update(collectionName: String, filter: Document, newDocument: Document) {
        getCollection(collectionName).replaceOne(filter, newDocument, ReplaceOptions().upsert(true)).subscribe(object : Subscriber<UpdateResult> {
            override fun onSubscribe(subscription: Subscription) {
                subscription.request(1)
            }

            override fun onNext(result: UpdateResult) {
                println("[rStream] A document was successfully updated in the database!")
            }

            override fun onError(t: Throwable) {
                println("[rStream] Error updating document: ${t.message}")
            }

            override fun onComplete() {}
        })
    }

    fun query(collectionName: String, filter: Document, callback: (List<Document>) -> Unit) {
        val results = mutableListOf<Document>()
        getCollection(collectionName).find(filter).subscribe(object : Subscriber<Document> {
            override fun onSubscribe(subscription: Subscription) {
                subscription.request(Long.MAX_VALUE)
            }

            override fun onNext(result: Document) {
                results.add(result)
            }

            override fun onError(t: Throwable) {
                println("[rStream] Error querying documents: ${t.message}")
            }

            override fun onComplete() {
                callback(results)
            }
        })
    }

}