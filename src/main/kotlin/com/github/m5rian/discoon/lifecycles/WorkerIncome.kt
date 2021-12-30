package com.github.m5rian.discoon.lifecycles

import com.github.m5rian.discoon.database.playerOrNull
import com.github.m5rian.discoon.utilities.lifeCycleCoroutine
import dev.minn.jda.ktx.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Responsible for starting and stopping a players workers.
 */
object WorkerIncome : ListenerAdapter() {

    val workScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * Runs as soon as the bot is online.
     * Starts registration of commands.
     */
    override fun onReady(event: ReadyEvent) {
        lifeCycleCoroutine.launch {
            val members: List<Member> = event.jda.guilds.flatMap { it.loadMembers().await() }
            members
                .filter { it.onlineStatus != OnlineStatus.OFFLINE }
                .mapNotNull { it.playerOrNull }
                .forEach { it.startIncome() }
        }
    }

    /**
     * Starts all workers when a user goes online.
     * Stops all workers when a user goes offline.
     *
     * @param event The UserUpdateOnlineStatusEvent.
     */
    override fun onUserUpdateOnlineStatus(event: UserUpdateOnlineStatusEvent) {
        // User went online
        if (event.oldOnlineStatus == OnlineStatus.OFFLINE) {
            event.member.playerOrNull?.startIncome()
        }
        // User went offline
        else if (event.newOnlineStatus == OnlineStatus.OFFLINE) {
            event.member.playerOrNull?.stopIncome()
        }
    }

}