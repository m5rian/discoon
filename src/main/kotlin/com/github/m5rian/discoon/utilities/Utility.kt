package com.github.m5rian.discoon.utilities

import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.utilities.cooldown.Cooldown
import com.github.m5rian.kotlingua.Kotlingua
import com.github.m5rian.kotlingua.Lang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import java.util.*

val XMAS get() = Lang("xmas", "Christmas")

val lang: Lang
    get() {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val month = calendar.get(Calendar.MONTH)
        return if (month > 10 || month == 0) XMAS
        else Kotlingua.defaultLang
    }

data class GuildMember(val guildId: String, val userId: String)

val lifeCycleCoroutine = CoroutineScope(Dispatchers.Default)
val commandsCoroutine = CoroutineScope(Dispatchers.Default)
val listenerCoroutine = CoroutineScope(Dispatchers.Default)
fun Double.format(): String = "%.2f".format(this)

val Int.inMinutes: Long get() = this * 1000L * 60L

fun countTiers(workers: MutableList<Worker>): Map<Short, Int> {
    val workers: MutableList<Worker> = workers.apply { this.sortBy { it.tier } } // Sort workers by tiers
    val workerTiers: List<Short> = workers.map { it.tier }.distinct() // Get a list of all worker tiers
    val tierCount = mutableMapOf<Short, Int>() // Create map for tier - amount tracking
    workerTiers.forEach { tier ->
        tierCount[tier] = workers.count { it.tier == tier }
    }
    return tierCount
}

fun ReplyAction.queueWithCooldown(member: Member, cooldown: Cooldown) {
    val guildMember = GuildMember(member.guild.id, member.id)
    this.queue {
        /*
        it.retrieveOriginal().queue {
            Upgrade.openMenus[guildMember] = it.jumpUrl
        }
         */
        commandsCoroutine.launch {
            kotlinx.coroutines.delay(1.inMinutes)
            it.retrieveOriginal().queue { msg -> cooldown.onCooldownExpire(msg) }
        }
    }
}