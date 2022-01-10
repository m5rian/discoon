package com.github.m5rian.discoon.commands

import com.github.m5rian.discoon.commands.impl.Workers
import com.github.m5rian.discoon.utilities.GuildMember
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

interface Command {

    suspend fun onCommand(event: SlashCommandEvent)

    private fun beforeCommand(event: SlashCommandEvent) {
        val guildMember = GuildMember(event.guild!!.id, event.member!!.id)

        if (event.name != "workers" && Workers.openMenus.containsKey(guildMember)) {
            val url = Workers.openMenus[guildMember]!!.split("/")
            val messageId = url[url.size - 1]
            val channelId = url[url.size - 2]
            val guildId = url[url.size - 3]
            event.guild!!.getTextChannelById(channelId)!!.retrieveMessageById(messageId).queue {
                Workers.closeMenu(guildMember, it)
            }
        }
    }

    suspend fun runCommandExecution(event: SlashCommandEvent) {
        beforeCommand(event)
        onCommand(event)
    }

}