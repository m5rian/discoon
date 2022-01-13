package com.github.m5rian.discoon.commands

import com.github.m5rian.discoon.commands.impl.*
import dev.minn.jda.ktx.interactions.Command
import dev.minn.jda.ktx.interactions.updateCommands
import dev.minn.jda.ktx.onCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.time.ExperimentalTime

object CommandManager : ListenerAdapter() {

    /**
     * Runs as soon as the bot is online.
     * Starts registration of commands.
     */
    @OptIn(ExperimentalTime::class)
    override fun onReady(event: ReadyEvent) {
        //registerCommands(event.jda)
        registerCallbacks(event.jda)
    }

    /**
     * Registers all commands to Discord
     *
     * @param jda instance of jda object
     */
    private fun registerCommands(jda: JDA) {
        jda.updateCommands {
            addCommands(
                Command("hire", "\uD83D\uDCBC Hire a new slave"),
                Command("jobs", "Where are you going?"),
                Command("workers", "\uD83D\uDC77 Get a list of all of your workers"),
                Command("stats", "\uD83D\uDCC8 Check the stats of your business"),
                Command("manager", "idk yet")
            )
        }.queue()
    }

    @ExperimentalTime
    private fun registerCallbacks(jda: JDA) {
        jda.onCommand("hire") { Hire.runCommandExecution(it) }
        jda.onCommand("jobs") { OpenJobMenu.runCommandExecution(it) }
        jda.onCommand("workers") { Workers.runCommandExecution(it) }
        jda.onCommand("upgrade") { Upgrade.runCommandExecution(it) }
        jda.onCommand("assign") { Assign.runCommandExecution(it) }
        jda.onCommand("stats") { Stats.runCommandExecution(it) }
    }

}
