package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.Player
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.enteties.workers.WorkerTier
import com.github.m5rian.discoon.enteties.workers.workerTiers
import com.github.m5rian.discoon.utilities.*
import com.github.m5rian.discoon.utilities.cooldown.ComponentClearCooldown
import dev.minn.jda.ktx.interactions.SelectionMenu
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

/**
 * Upgrade you workers.
 */
object Upgrade : Command {

    /**
     * Stores the selected Tier of the upgrade list per user.
     */
    private val selectedTier = mutableMapOf<String, WorkerTier>()

    override suspend fun onCommand(event: SlashCommandEvent) {
        /*
        val guildMember = GuildMember(event.guild!!.id, event.member!!.id)
        if (openMenus.containsKey(guildMember)) {
            event.reply(true) {
                text = "workers.alreadyOpened"
                variables = {
                    arg("jumpUrl", openMenus[guildMember]!!)
                }
            }.queue()
            return
        }
         */

        val player = event.member!!.player
        // Player has no workers
        if (player.workers.isEmpty()) {
            event.reply { text = "noWorkers" }.queue()
        }
        // Player owns at least one worker
        else {
            val selectTier = SelectionMenu(generateComponentId().toString()) {
                this.placeholder = lang.get("workers.selection.placeholder")
                countTiers(player.workers).forEach { (tier, _) ->
                        println(tier)
                        val label = lang.get("workers.selection.tier") { it.arg("tier", tier) }
                        addOption(label, tier.toString())
                }
                onClick(event.user) { onWorkerTierSelect(it) }
            }
            val upgradeOne = Button.primary(generateComponentId().toString(), lang.get("workers.upgrade.1"))
                .asDisabled()
                .onClick(event.user) { onWorkerUpgrade(it, 1) }
            val upgradeTen = Button.primary(generateComponentId().toString(), lang.get("workers.upgrade.10"))
                .asDisabled()
                .onClick(event.user) { onWorkerUpgrade(it, 10) }
            val upgradeHundred = Button.primary(generateComponentId().toString(), lang.get("workers.upgrade.100"))
                .asDisabled()
                .onClick(event.user) { onWorkerUpgrade(it, 100) }

            event.reply {
                text = getMessage(player)
                transform = false
            }
                .addActionRow(selectTier)
                .addActionRow(upgradeOne, upgradeTen, upgradeHundred)
                .queueWithCooldown(event.member!!, ComponentClearCooldown)
        }
    }

    private suspend fun getMessage(player: Player): String {
        var text = lang.get("workers.total") { it.arg("workers", player.workers.size) }

        val tierCount = countTiers(player.workers)
        tierCount.forEach { (tier, amount) ->
            text += lang.get("workers.tier") {
                it.arg("tier", tier)
                it.arg("amount", amount)
            }
        }

        return text
    }

    /**
     * Enables or disables the button whether the user is allowed to purchase the number of upgrades specified in the button label.
     *
     * @param event The [SelectionMenuEvent].
     */
    @Suppress("DuplicatedCode")
    private fun onWorkerTierSelect(event: SelectionMenuEvent) {
        val selectedTier: WorkerTier = event.selectedOptions!![0].value.toShort().let { tier -> // Get the selected tier
            workerTiers.first { it.tier == tier }
        }
        this.selectedTier[event.user.id] = selectedTier

        val player = event.member!!.player
        val workers: MutableList<Worker> = player.workers
        val upgradableWorkers = workers.count { it.tier == selectedTier.tier }

        val menu: SelectionMenu = event.selectionMenu!!.createCopy().setDefaultOptions(event.selectedOptions ?: emptyList()).build()
        val upgradeButtons: List<Button> = event.message.actionRows[1].buttons.map { button ->
            val words: List<String> = button.label.split("\\s+".toRegex()) // Split name of button in spaces
            val minCount: String = words[1].substring(0, words[1].length - 1) // Remove last character of words

            if (upgradableWorkers < minCount.toInt()) button.asDisabled()
            else button.asEnabled()
        }
        event.editComponents(ActionRow.of(menu), ActionRow.of(upgradeButtons)).queue()
    }

    private suspend fun onWorkerUpgrade(event: ButtonClickEvent, assignAmount: Int) {
        val selectedTier: WorkerTier = selectedTier[event.user.id]!!
        val price = selectedTier.price * assignAmount
        val player = event.member!!.player

        // Player has enough money for upgrading
        if (player.balance >= price) {
            player.removeBalance(price)
            val workersToUpgrade: List<Worker> = player.workers
                .filter { it.tier == selectedTier.tier }
                .slice(0 until assignAmount) // Get a worker matching the tier to upgrade
            workersToUpgrade.forEach { worker ->
                player.removeWorker(worker) // Remove old worker (with old tier)
                worker.tier++ // Increase tier of current worker by 1
                player.addWorker(worker) // Add worker with upgraded tier
            }
            event.addToast("workers.upgraded").queue()
        }
        // Player hasn't enough money for upgrading
        else {
            event.reply {
                text = "workers.missingMoney"
                variables = {
                    arg("balance", player.balance.format())
                    arg("price", price.format())
                }
            }.queue()
        }
    }

}