package com.github.m5rian.discoon.commands

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

interface Command {

    suspend fun onCommand(event: SlashCommandEvent)

}