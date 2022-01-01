package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.managers.Manager
import com.github.m5rian.discoon.enteties.managers.ManagerTiers
import com.github.m5rian.discoon.enteties.workers.WorkerTiers
import com.github.m5rian.discoon.utilities.*
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.Button
import kotlin.time.ExperimentalTime

/**
 * Hire a single worker of type [WorkerTiers.ONE].
 */
object Hire : Command {

    @ExperimentalTime
    override suspend fun onCommand(event: SlashCommandEvent) {
        val player = event.member!!.player

        // Bought worker
        if (player.balance >= WorkerTiers.ONE.price) {
            val hireWorker = Button.primary(ButtonManager.generateId().toString(), "Hire worker")
            val hireManager = Button.primary(ButtonManager.generateId().toString(), "Hire manager")
            event.reply {
                text = """
                Buy a new worker or manager!
                **:construction_worker:Worker:** `${WorkerTiers.ONE.price}$`
                **:office_worker:Manager:** `2500$`
            """.trimIndent()
            }.addActionRow(
                hireWorker.onClick(event.user) { onWorkerHire(it) },
                hireManager.onClick(event.user) { onManagerHire(it) }
            ).queue()
        }
        // Not enough money
        else {
            event.reply {
                text = "You lil piece of shit, tried to scam me?\nYou don't have enough money"
            }.queue()
        }

    }

    @Suppress("DuplicatedCode")
    private suspend fun onWorkerHire(event: ButtonClickEvent) {
        event.disableButtons() // Disable buttons on original message
        val player = event.member!!.player
        player.removeBalance(WorkerTiers.ONE.price) // Buy worker
        player.addWorker(1).startIncome(player) // Create new worker
        event.reply {
            text = """
                __**:tada: Hired worker**__
                :scroll: Worker tier: `Tier 1`
                :dollar: Costs: `-${WorkerTiers.ONE.price}$`
                :moneybag: Balance: `${player.balance.format()}$`
            """.trimIndent()
        }.queue()
    }

    @Suppress("DuplicatedCode")
    private suspend fun onManagerHire(event: ButtonClickEvent) {
        event.disableButtons() // Disable buttons on original message
        val player = event.member!!.player
        player.removeBalance(ManagerTiers.ONE.price) // Buy manager
        player.addManager()
        event.reply {
            text = """
                __**:tada: Hired manager**__
                :scroll: Status: **unassigned**
                :dollar: Costs: `-${ManagerTiers.ONE.price}$`
                :moneybag: Balance: `${player.balance.format()}$`
            """.trimIndent()
        }.queue()
    }

}