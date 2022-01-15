package com.github.m5rian.discoon.commands

import com.github.m5rian.discoon.utilities.GuildMember
import com.github.m5rian.discoon.utilities.commandsCoroutine
import com.github.m5rian.discoon.utilities.cooldown.Cooldown
import com.github.m5rian.discoon.utilities.deleteComponents
import com.github.m5rian.discoon.utilities.inMinutes
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction

val lastCommand = mutableMapOf<GuildMember, String>()

interface Command {

    suspend fun onCommand(event: SlashCommandEvent)

    private fun beforeCommand(event: SlashCommandEvent) {
        val guildMember = GuildMember(event.guild!!.id, event.member!!.id)
        if (lastCommand.containsKey(guildMember)) {
            val url = lastCommand[guildMember]!!.split("/")
            val messageId = url[url.size - 1]
            val channelId = url[url.size - 2]
            val guildId = url[url.size - 3]

            lastCommand.remove(guildMember)
            event.guild!!.getTextChannelById(channelId)!!.retrieveMessageById(messageId).queue(Message::deleteComponents)
        }
    }

    suspend fun runCommandExecution(event: SlashCommandEvent) {
        beforeCommand(event)
        onCommand(event)
    }

}

fun ReplyAction.queue(hasMenu: Boolean = false, cooldown: Cooldown? = null) {
    this.queue {
        if (hasMenu) {
            it.retrieveOriginal().queue { message ->
                val member = GuildMember(it.interaction.guild!!.id, it.interaction.member!!.id)
                lastCommand[member] = message.jumpUrl
            }
        }

        if (cooldown != null) {
            commandsCoroutine.launch {
                kotlinx.coroutines.delay(1.inMinutes)
                it.retrieveOriginal().queue { msg -> cooldown.onCooldownExpire(msg) }
            }
        }
    }
}