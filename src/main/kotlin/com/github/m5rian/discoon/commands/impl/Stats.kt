package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.utilities.format
import com.github.m5rian.discoon.utilities.reply
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

object Stats : Command {

    override suspend fun onCommand(event: SlashCommandEvent) {
        val balance = event.member!!.player.balance
        val workers = event.member!!.player.workers

        event.reply {
            text = """
                :moneybag:Balance: `${balance.format()}$`
                :construction_worker: `${workers.size}`
            """.trimIndent()
        }.queue()
    }

}