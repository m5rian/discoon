package com.github.m5rian.discoon.database

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Member
import org.bson.conversions.Bson
import org.litote.kmongo.*

data class Player(
        val userId: String,
        val guildId: String,

        var balance: Double,
        var workers: MutableList<Worker>
) {
    /**
     * Starts the income of all workers.
     */
    fun startIncome() {
        this.workers.forEach { it.startIncome(this) }
    }

    /**
     * Stops the income of all workers
     */
    fun stopIncome() {
        this.workers.forEach { it.stopIncome() }
    }

    private val mutex: Mutex = Mutex()

    private suspend fun update(db: Bson, cache: (Player) -> Unit) {
        cache.invoke(this)
        mutex.withLock {
            Mongo.getAs<Player>("players").updateOne(and(Player::userId eq userId, Player::guildId eq guildId), db)
        }
    }

    suspend fun removeBalance(amount: Double) = update(inc(Player::balance, -amount)) { it.balance -= amount }
    suspend fun addBalance(amount: Double) = update(inc(Player::balance, amount)) { it.balance += amount }
    suspend fun addWorker(worker: Worker) = update(push(Player::workers, worker)) { it.workers.add(worker) }
    suspend fun removeWorker(worker: Worker) = update(pull(Player::workers, worker)) { it.workers.remove(worker) }
}

val Member.playerOrNull: Player? get() = Mongo.getAs<Player>("players").findOne(and(Player::userId eq this.id, Player::guildId eq this.guild.id))


val Member.player: Player
    get() = Mongo.getAs<Player>("players").findOne(and(Player::userId eq this.id, Player::guildId eq this.guild.id))
        ?: run {
            val player = Player(
                userId = this.id,
                guildId = this.guild.id,
                balance = 1000.0,
                workers = mutableListOf()
            )
            return player.also { Mongo.getAs<Player>("players").insertOne(player) }
        }