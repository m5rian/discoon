package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.Player
import com.github.m5rian.discoon.database.Worker
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.WorkerTiers
import com.github.m5rian.discoon.utilities.reply
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

/**
 * Hire a single worker of type [WorkerTiers.ONE].
 */
object Hire : Command {

    override suspend fun onCommand(event: SlashCommandEvent) {
        val player = event.member!!.player

        // Bought worker
        if (player.balance >= WorkerTiers.ONE.price) {
            player.removeBalance(WorkerTiers.ONE.price)

            val worker = Worker(1)
            player.addWorker(worker)
            worker.startIncome(player)

            event.reply { text = ":tada: You successfully bought a worker `Tier 1`!" }.queue()
        }
        // Not enough money
        else {
            event.reply {
                text = "You lil piece of shit, tried to scam me?\nYou don't have enough money"
            }.queue()
        }

    }

}