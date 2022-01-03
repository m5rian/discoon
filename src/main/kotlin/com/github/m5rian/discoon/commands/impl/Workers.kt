package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.utilities.SelectionMenuManager
import com.github.m5rian.discoon.utilities.onClick
import com.github.m5rian.discoon.utilities.reply
import dev.minn.jda.ktx.interactions.SelectionMenu
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

/**
 * Provides detail about your workers.
 * Possibility to upgrade workers.
 */
object Workers : Command {

    override suspend fun onCommand(event: SlashCommandEvent) {
        val workers: MutableList<Worker> = event.member!!.player.workers.apply { this.sortBy { it.tier } } // Sort workers by tiers

        val workerTiers: List<Short> = workers.map { it.tier }.distinct() // Get a list of all worker tiers
        val tierCount = mutableMapOf<Short, Int>() // Create map for tier - amount tracking
        workerTiers.forEach { tier ->
            tierCount[tier] = workers.count { it.tier == tier }
        }

        // Player has no workers
        if (workers.isEmpty()) {
            event.reply { text = ":warning:You don't have any workers! Use `/hire` to buy one!" }.queue()
        }
        // Player owns at least one worker
        else {
            val message = StringBuilder(":factory_worker:Workers: `${workers.size}`")
            tierCount.forEach { (tier, amount) -> message.append("\n**Tier $tier**: `$amount`") }
            event.reply { text = message.toString() }
                .addActionRow(SelectionMenu(SelectionMenuManager.generateId().toString()) {
                    this.placeholder = "Upgrade a worker"
                    workerTiers.forEach { addOption("Tier $it", it.toString()) }
                    onClick(event.user) {
                        val selectedTier = it.selectedOptions!![0].value.toShort()
                        val workerToUpgrade = it.member!!.player.workers.first { worker -> worker.tier == selectedTier }
                    }
                }).queue()
        }
    }

}