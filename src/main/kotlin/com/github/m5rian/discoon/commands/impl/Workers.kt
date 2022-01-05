package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.player
import com.github.m5rian.discoon.enteties.workers.Worker
import com.github.m5rian.discoon.enteties.workers.WorkerTier
import com.github.m5rian.discoon.enteties.workers.workerTiers
import com.github.m5rian.discoon.utilities.*
import dev.minn.jda.ktx.interactions.SelectionMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private val openMenus = mutableMapOf<GuildMember, String>()

    /**
     * Stores the selected Tier of the upgrade list per user.
     */
    private val selectedTier = mutableMapOf<String, WorkerTier>()

    override suspend fun onCommand(event: SlashCommandEvent) {
        val guildMember = GuildMember(event.guild!!.id, event.member!!.id)
        if (openMenus.containsKey(guildMember)) {
            event.reply(true) { text = "You have already opened the worker menu! Jump to [the already opened menu](${openMenus[guildMember]})!" }.queue()
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
            event.reply { text = ":warning:You don't have any workers! Use `/hire` to buy one!" }.queue()
        }
        // Player owns at least one worker
        else {
            val message = StringBuilder(":factory_worker:Workers: `${workers.size}`")

            val menu = SelectionMenu(generateComponentId().toString()) {
                this.placeholder = "Upgrade a worker"
                workerTiers.forEach { addOption("Tier $it", it.toString()) }
                onClick(event.user) { onWorkerTierSelect(it) }
            }
            val upgradeOne = Button.primary(generateComponentId().toString(), "Upgrade 1x")
                .asDisabled()
                .onClick(event.user) { onButtonClick(it, 1) }
            val upgradeTen = Button.primary(generateComponentId().toString(), "Upgrade 10x")
                .asDisabled()
                .onClick(event.user) { onButtonClick(it, 10) }
            val upgradeHundred = Button.primary(generateComponentId().toString(), "Upgrade 100x")
                .asDisabled()
                .onClick(event.user) { onButtonClick(it, 100) }

            tierCount.forEach { (tier, amount) -> message.append("\n**Tier $tier**: `$amount`") }
            event.reply { text = message.toString() }
                .addActionRow(menu)
                .addActionRow(upgradeOne, upgradeTen, upgradeHundred)
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
            hook.retrieveOriginal().queue { it.editMessageComponents().queue() }
            openMenus.remove(guildMember)
        }
    }

    /**
     * Checks if a button must be disabled because of missing workers to upgrade.
     *
     */
    private fun Button.checkIfButtonIsDisabled(sizeOfEntities: Int, minSize: Int): Button {
        return if (sizeOfEntities < minSize) this.asDisabled()
        else this.asEnabled()
    }

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
        val workers: MutableList<Worker> = event.member!!.player.workers
        val amountOfTiersOfSelectedTier = workers.count { it.tier == selectedTier.tier }

        val menu: SelectionMenu = event.selectionMenu!!.createCopy().setDefaultOptions(event.selectedOptions ?: emptyList()).build()
        val buttons: List<Button> = event.message.actionRows[1].buttons.map { button ->
            val words: List<String> = button.label.split("\\s+".toRegex()) // Split name of button in spaces
            val minCount: String = words[1].substring(0, words[1].length - 1) // Remove last character of words

            button.checkIfButtonIsDisabled(amountOfTiersOfSelectedTier, minCount.toInt())
        }
        event.editComponents(ActionRow.of(menu), ActionRow.of(buttons)).queue()
    }

    private suspend fun onButtonClick(event: ButtonClickEvent, upgradeAmount: Int) {
        val selectedTier: WorkerTier = selectedTier[event.user.id]!!
        val price = selectedTier.price * upgradeAmount
        val player = event.member!!.player

        // Player has enough money for upgrading
        if (player.balance >= price) {
            player.removeBalance(price)
            val workerToUpgrade: Worker = player.workers.first { it.tier == selectedTier.tier } // Get a worker matching the tier to upgrade
            player.removeWorker(workerToUpgrade) // Remove old worker (with old tier)
            workerToUpgrade.tier++ // Increase tier of current worker by 1
            player.addWorker(workerToUpgrade) // Add worker with upgraded tier
            event.addToast("**You have completed the upgrading process :thumbsup:**").queue()
        }
        // Player hasn't enough money for upgrading
        else {
            event.reply { text = "You don't have enough money to upgrade!:middle_finger:" }.queue()
        }
    }

}