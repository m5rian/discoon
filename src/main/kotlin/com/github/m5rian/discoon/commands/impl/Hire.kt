package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.config
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.managers.ManagerTiers
import com.github.m5rian.discoon.enteties.workers.workerTiers
import com.github.m5rian.discoon.utilities.*
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.Button
import kotlin.time.ExperimentalTime

/**
 * Hire a single tier 1 worker.
 */
object Hire : Command {

    @ExperimentalTime
    override suspend fun onCommand(event: SlashCommandEvent) {
        val player = event.member!!.player

        // Bought worker
        if (player.balance >= workerTiers[0].price) {
            val hireWorker = Button.primary(generateComponentId().toString(), lang.get("hire.worker")).apply {
                if (player.balance < workerTiers[0].price) this.asDisabled()
                else onClick(event.user) { onWorkerHire(it) }
            }
            val hireManager = Button.primary(generateComponentId().toString(), lang.get("hire.manager")).apply {
                if (player.balance < ManagerTiers.ONE.price) this.asDisabled()
                else onClick(event.user) { onManagerHire(it) }
            }
            event.reply {
                text = "hire.buyList"
                variables = {
                    arg("price", workerTiers[0].price)
                }
            }.addActionRow(hireWorker, hireManager).queue()
        }
        // Not enough money
        else event.reply {
            text = "hire.missingMoney"
            colour = config.red
        }.queue()

    }

    @Suppress("DuplicatedCode")
    private suspend fun onWorkerHire(event: ButtonClickEvent) {
        event.disableButtons() // Disable buttons on original message
        val player = event.member!!.player
        player.removeBalance(workerTiers[0].price) // Buy worker
        player.addWorker(1).startIncome(player) // Create new worker
        event.reply {
            text = "hire.bought.worker"
            colour = config.green
            variables = {
                arg("price", workerTiers[0].price)
                arg("balance", player.balance.format())
            }
        }.queue()
    }

    @Suppress("DuplicatedCode")
    private suspend fun onManagerHire(event: ButtonClickEvent) {
        event.disableButtons() // Disable buttons on original message
        val player = event.member!!.player
        player.removeBalance(ManagerTiers.ONE.price) // Buy manager
        player.addManager()
        event.reply {
            text = "hire.bought.manager"
            colour = config.green
            variables = {
                arg("price", ManagerTiers.ONE.price)
                arg("balance", player.balance.format())
            }
        }.queue()
    }

}