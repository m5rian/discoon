package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.managers.Manager
import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.enteties.workers.WorkerTier
import com.github.m5rian.discoon.enteties.workers.workerTiers
import com.github.m5rian.discoon.utilities.*
import dev.minn.jda.ktx.interactions.SelectionMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

/**
 * Provides detail about your workers.
 * Possibility to upgrade workers.
 */
object Workers : Command {

    val openMenus = mutableMapOf<GuildMember, String>()

    /**
     * Stores the selected Tier of the upgrade list per user.
     */
    private val selectedTier = mutableMapOf<String, WorkerTier>()

    override suspend fun onCommand(event: SlashCommandEvent) {
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

        val workers: MutableList<Worker> = event.member!!.player.workers.apply { this.sortBy { it.tier } } // Sort workers by tiers

        val workerTiers: List<Short> = workers.map { it.tier }.distinct() // Get a list of all worker tiers
        val tierCount = mutableMapOf<Short, Int>() // Create map for tier - amount tracking
        workerTiers.forEach { tier ->
            tierCount[tier] = workers.count { it.tier == tier }
        }

        // Player has no workers
        if (workers.isEmpty()) {
            event.reply { text = "workers.noWorkers" }.queue()
        }
        // Player owns at least one worker
        else {
            val message = StringBuilder(lang.get("workers.total") { it.arg("workers", workers.size) })
            tierCount.forEach { (tier, amount) ->
                message.append(lang.get("workers.tier") {
                    it.arg("tier", tier)
                    it.arg("amount", amount)
                })
            }

            val menu = SelectionMenu(generateComponentId().toString()) {
                this.placeholder = lang.get("workers.selection.placeholder")
                workerTiers.forEach { tier -> addOption(lang.get("workers.selection.tier") { it.arg("tier", tier) }, tier.toString()) }
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
            val assignOne = Button.primary(generateComponentId().toString(), lang.get("workers.assign.1"))
                .asDisabled()
                .onClick(event.user) { onManagerAssign(it, 1) }
            val assignTen = Button.primary(generateComponentId().toString(), lang.get("workers.assign.10"))
                .asDisabled()
                .onClick(event.user) { onManagerAssign(it, 10) }
            val assignHundred = Button.primary(generateComponentId().toString(), lang.get("workers.assign.100"))
                .asDisabled()
                .onClick(event.user) { onManagerAssign(it, 100) }

            event.reply {
                text = message.toString()
                transform = false
            }
                .addActionRow(menu)
                .addActionRow(upgradeOne, upgradeTen, upgradeHundred)
                .addActionRow(assignOne, assignTen, assignHundred)
                .queue { cooldown(guildMember, it) }
        }
    }

    /**
     * Runs a cooldown after which the interaction components get removed.
     * Also removes the menu from the [openMenus] map.
     *
     * @param guildMember The member who owns the menu.
     * @param hook Interaction hook menu sent.
     */
    private fun cooldown(guildMember: GuildMember, hook: InteractionHook) {
        hook.retrieveOriginal().queue {
            openMenus[guildMember] = it.jumpUrl
        }
        commandsCoroutine.launch {
            delay(1.inMinutes)
            hook.retrieveOriginal().queue {
                closeMenu(guildMember, it)
            }
        }
    }

    fun closeMenu(guildMember: GuildMember, message: Message) {
        message.deleteComponents()
        openMenus.remove(guildMember)
    }

    /**
     * Checks if a button must be disabled because of missing entities.
     * This can be workers to upgrade
     * or
     * managers to assign.
     */
    private fun Button.mustBeDisabled(sizeOfEntities: Int, minSize: Int): Boolean = sizeOfEntities < minSize


    /**
     * Enables the button which shows if you can upgrade
     * <vs.> :^)
     * Enables or disables the button whether the user is allowed to purchase the number of upgrades specified in the button label.
     *
     * @param event The [SelectionMenuEvent].
     */
    @Suppress("DuplicatedCode")
    fun onWorkerTierSelect(event: SelectionMenuEvent) {
        val selectedTier: WorkerTier = event.selectedOptions!![0].value.toShort().let { tier -> // Get the selected tier
            workerTiers.first { it.tier == tier }
        }
        this.selectedTier[event.user.id] = selectedTier

        val player = event.member!!.player
        val workers: MutableList<Worker> = player.workers
        val upgradableWorkers = workers.count { it.tier == selectedTier.tier }
        val unassignedWorkers: List<Manager> = player.managers.filter { it.assignedTo == null }

        val menu: SelectionMenu = event.selectionMenu!!.createCopy().setDefaultOptions(event.selectedOptions ?: emptyList()).build()
        val upgradeButtons: List<Button> = event.message.actionRows[1].buttons.map { button ->
            val words: List<String> = button.label.split("\\s+".toRegex()) // Split name of button in spaces
            val minCount: String = words[1].substring(0, words[1].length - 1) // Remove last character of words

            if (button.mustBeDisabled(upgradableWorkers, minCount.toInt())) button.asDisabled()
            else button.asEnabled()
        }
        val assignButtons: List<Button> = event.message.actionRows[2].buttons.map { button ->
            val words: List<String> = button.label.split("\\s+".toRegex()) // Split name of button in spaces
            val minCount: String = words[1].substring(0, words[1].length - 1) // Remove last character of words

            if (button.mustBeDisabled(unassignedWorkers.size, minCount.toInt()) || button.mustBeDisabled(upgradableWorkers, minCount.toInt())) {
                button.asDisabled()
            } else button.asEnabled()
        }
        event.editComponents(ActionRow.of(menu), ActionRow.of(upgradeButtons), ActionRow.of(assignButtons)).queue()
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

    private suspend fun onManagerAssign(event: ButtonClickEvent, upgradeAmount: Int) {
        val selectedTier: WorkerTier = Workers.selectedTier[event.user.id]!!
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

            event.addToast("workers.assigned").queue()
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