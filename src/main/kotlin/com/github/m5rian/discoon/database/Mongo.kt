package com.github.m5rian.discoon.database

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.CoroutineScope
import org.bson.Document
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

object Mongo {
    lateinit var connectionString: String
    lateinit var database: String
    lateinit var coroutineScope: CoroutineScope

    lateinit var mongo: MongoDatabase

    fun connect() {
        mongo = KMongo.createClient(connectionString).getDatabase(database)
    }

    fun get(collection: String): MongoCollection<Document> {
        return mongo.getCollection(collection)
    }

    inline fun <reified T : Any> getAs(collection: String): MongoCollection<T> {
        return mongo.getCollection<T>(collection)
    }

    fun connectionStringIsInitialized(): Boolean {
        return this::connectionString.isInitialized
    }

}

fun database(database: Mongo.() -> Unit) = Mongo.apply(database)