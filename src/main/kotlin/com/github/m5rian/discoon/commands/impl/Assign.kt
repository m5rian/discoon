package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.commands.queue
import com.github.m5rian.discoon.database.Player
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.managers.Manager
import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.enteties.workers.WorkerTier
import com.github.m5rian.discoon.enteties.workers.workerTiers
import com.github.m5rian.discoon.utilities.*
import com.github.m5rian.discoon.utilities.cooldown.ComponentClearCooldown
import dev.minn.jda.ktx.interactions.SelectionMenu
import dev.minn.jda.ktx.messages.editMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

object Assign : Command {

    /**
     * Stores the selected Tier of the upgrade list per user.
     */
    private val selectedTier = mutableMapOf<String, WorkerTier>()

    override suspend fun onCommand(event: SlashCommandEvent) {
        val player = event.member!!.player

        // Player has no workers
        if (player.workers.isEmpty()) {
            event.reply { text = "noWorkers" }.queue()
        }
        // Player has no managers
        else if (player.managers.isEmpty()) {
            event.reply { text = "noManagers" }.queue()
        }
        // Pla|yer owns at least one worker and manager
        else {
            event.reply {
                text = getMessage(player)
                components = getComponents(player, event.user)
                transform = false
            }.queue(true, ComponentClearCooldown)
        }
    }

    private suspend fun getMessage(player: Player): String {
        val tierCount = countTiers(player.workers)

        var text = lang.get("assign.total") {
            it.arg("balance", player.balance.format())
            it.arg("unassignedManagers", player.managers.count { manager -> manager.assignedTo == null })
            it.arg("managers", player.managers.size)
            it.arg("unassignedWorkers", player.workers.count { worker ->
                player.managers.none { manager -> manager.assignedTo == worker.id }
            })
            it.arg("workers", player.workers.size)
        }

        tierCount.forEach { (tier, amount) ->
            text += lang.get("assign.worker") {
                it.arg("tier", tier)
                it.arg("notManagedWorkers", player.workers
                    .filter { worker -> worker.tier == tier }
                    .count { worker -> player.managers.none { manager -> manager.assignedTo == worker.id } }
                )
                it.arg("workers", amount)
                it.arg("price", workerTiers.first { workerTier -> workerTier.tier == tier }.price.format())
            }
        }

        return text
    }

    private suspend fun getComponents(player: Player, user: User): List<ActionRow> {
        val selectTier = SelectionMenu(generateComponentId().toString()) {
            this.placeholder = lang.get("assign.selection.placeholder")
            countTiers(player.workers).forEach { (tier, _) -> addOption(lang.get("workers.selection.tier") { it.arg("tier", tier) }, tier.toString()) }
            onClick(user) { onWorkerTierSelect(it) }
        }
        val assignOne = Button.primary(generateComponentId().toString(), lang.get("workers.assign.1"))
            .asDisabled()
            .onClick(user) { onManagerAssign(it, 1) }
        val assignTen = Button.primary(generateComponentId().toString(), lang.get("workers.assign.10"))
            .asDisabled()
            .onClick(user) { onManagerAssign(it, 10) }
        val assignHundred = Button.primary(generateComponentId().toString(), lang.get("workers.assign.100"))
            .asDisabled()
            .onClick(user) { onManagerAssign(it, 100) }

        return listOf(ActionRow.of(selectTier), ActionRow.of(assignOne, assignTen, assignHundred))
    }

    /**
     * Enables or disables the button whether the user is allowed to purchase the number of upgrades specified in the button label.
     *
     * @param event The [SelectionMenuEvent].
     */
    @Suppress("DuplicatedCode")
    private suspend fun onWorkerTierSelect(event: SelectionMenuEvent) {
        val selectedTier: WorkerTier = event.selectedOptions!![0].value.toShort().let { tier -> // Get the selected tier
            workerTiers.first { it.tier == tier }
        }
        this.selectedTier[event.user.id] = selectedTier

        val player = event.member!!.player
        val workers: MutableList<Worker> = player.workers
        val upgradableWorkers = workers.filter { worker -> worker.tier == selectedTier.tier }
            .count { worker -> player.managers.none { manager -> manager.assignedTo == worker.id } }
        val unassignedWorkers: List<Manager> = player.managers.filter { it.assignedTo == null }

        val menu: SelectionMenu = event.selectionMenu!!.createCopy().setDefaultOptions(event.selectedOptions ?: emptyList()).build()
        val assignButtons: List<Button> = event.message.actionRows[1].buttons.map { button ->
            println("button")
            val words: List<String> = button.label.split("\\s+".toRegex()) // Split name of button in spaces
            val minCount: String = words[1].substring(0, words[1].length - 1) // Remove last character of words

            println(upgradableWorkers)
            println(minCount)
            if (unassignedWorkers.size < minCount.toInt() || upgradableWorkers < minCount.toInt()) button.asDisabled()
            else button.asEnabled()
        }
        event.editComponents(ActionRow.of(menu), ActionRow.of(assignButtons)).queue()
    }

    private suspend fun onManagerAssign(event: ButtonClickEvent, upgradeAmount: Int) {
        val selectedTier: WorkerTier = selectedTier[event.user.id]!!
        val price = selectedTier.price * upgradeAmount
        val player = event.member!!.player

        // Player has enough money for upgrading
        if (player.balance >= price) {
            player.removeBalance(price)

            val assignableWorkers = player.workers.filter { it.tier == selectedTier.tier }.slice(0 until upgradeAmount)
            val unassignedManagers = player.managers.filter { it.assignedTo == null }.slice(0 until upgradeAmount)

            unassignedManagers.forEachIndexed { i, manager ->
                player.removeManager(manager)
                manager.assignedTo = assignableWorkers[i].id
                player.addManager(manager)
            }

            val embed = EmbedBuilder(event.message.embeds[0]).apply {
                setDescription(getMessage(player))
            }.build()

            event.hook.editMessage(
                embed = embed,
                components = getComponents(player, event.user)
            ).queue()
            event.reply(true) { text = "assign.bought" }.queue()
        }
        // Player hasn't enough money for upgrading
        else {
            event.reply(true) {
                text = "workers.missingMoney"
                variables = {
                    arg("balance", player.balance.format())
                    arg("price", price.format())
                }
            }.queue()
        }
    }

}