package com.github.m5rian.discoon.database

import com.github.m5rian.discoon.enteties.managers.Manager
import com.github.m5rian.discoon.enteties.workers.Worker
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Member
import org.bson.conversions.Bson
import org.litote.kmongo.*
import java.util.*

data class Player(
        val userId: String,
        val guildId: String,

        var balance: Double,
        var workers: MutableList<Worker>,
        var managers: MutableList<Manager>
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

    private fun createId(ids: List<String>): String {
        val generatedId = UUID.randomUUID().toString()
        return if (!ids.contains(generatedId)) generatedId
        else createId(ids)
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
    suspend fun addWorker(worker: Worker): Worker = worker.also { update(push(Player::workers, worker)) { p -> p.workers.add(worker) } }
    suspend fun addWorker(tier: Short): Worker = Worker(createId(this.workers.map { it.id }), tier).also { update(push(Player::workers, it)) { p -> p.workers.add(it) } }
    suspend fun removeWorker(worker: Worker) = update(pull(Player::workers, worker)) { it.workers.remove(worker) }
    suspend fun addManager(): Manager = Manager(createId(this.managers.map { it.id })).also { update(push(Player::managers, it)) { p -> p.managers.add(it) } }
}

val Member.playerOrNull: Player? get() = Mongo.getAs<Player>("players").findOne(and(Player::userId eq this.id, Player::guildId eq this.guild.id))


val Member.player: Player
    get() = Mongo.getAs<Player>("players").findOne(and(Player::userId eq this.id, Player::guildId eq this.guild.id))
        ?: run {
            val player = Player(
                userId = this.id,
                guildId = this.guild.id,
                balance = 1000.0,
                workers = mutableListOf(),
                managers = mutableListOf()
            )
            return player.also { Mongo.getAs<Player>("players").insertOne(player) }
        }