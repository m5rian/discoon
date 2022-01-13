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
 */
object Workers : Command {
    override suspend fun onCommand(event: SlashCommandEvent) {
        event.reply("Why you're asking me? You are the owner of this businesses...").queue()
    }
}