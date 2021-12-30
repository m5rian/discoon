package com.github.m5rian.discoon

import kotlinx.serialization.Serializable

/**
 * Represents a config.json file, which is used to store secret or important data.
 * The configuration file gets decoded in [
 */
@Serializable
data class Config(
        val token: String, // Discord bot token
        val databaseConnection: String, // MongoDb connection String
        val baseIncome: Double // Base income per minute
)