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
                Command("ping", "Ping pong"),
                Command("hire", "Hire a new slave!"),
                Command("jobs", "Where are you going?"),
                Command("workers", "What are you doing?"),
                Command("stats", "Whats my Status?")
            )
        }.queue()
    }

    @ExperimentalTime
    private fun registerCallbacks(jda: JDA) {
        jda.onCommand("ping", null) { Ping.onCommand(it) }
        jda.onCommand("hire", null) { Hire.onCommand(it) }
        jda.onCommand("jobs", null) { OpenJobMenu.onCommand(it) }
        jda.onCommand("workers", null) { Workers.onCommand(it) }
        jda.onCommand("stats", null) { Stats.onCommand(it) }
    }

}
