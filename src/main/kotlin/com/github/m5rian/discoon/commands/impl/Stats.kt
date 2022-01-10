package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.utilities.format
import com.github.m5rian.discoon.utilities.reply
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

object Stats : Command {

    override suspend fun onCommand(event: SlashCommandEvent) {
        val player = event.member!!.player
        event.reply {
            text = "stats"
            variables = {
                arg("balance", player.balance.format())
                arg("workers", player.workers.size)
                arg("managers", player.managers.size)
                arg("unassignedManagers", player.managers.count { it.assignedTo == null })
            }
        }.queue()
    }

}