package com.github.m5rian.discoon.commands.impl

import com.github.m5rian.discoon.commands.Command
import com.github.m5rian.discoon.database.Mongo
import com.github.m5rian.discoon.database.Player
import com.github.m5rian.discoon.utilities.format
import com.github.m5rian.discoon.utilities.reply
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.litote.kmongo.eq

object Leaderboard : Command {
    override suspend fun onCommand(event: SlashCommandEvent) {
        val players = Mongo.getAs<Player>("players")
            .find(Player::guildId eq event.guild!!.id)
            .limit(10)

        val ids = players.map { it.userId }.toList().toTypedArray()
        val members = event.guild!!.retrieveMembersByIds(*ids).await()

        var leaderboard = ""
        players.sortedBy { it.balance }.asReversed().forEachIndexed { i, database ->
            val discord = members.first { it.id == database.userId }

            leaderboard += when (i) {
                0 -> "${i + 1} \uD83E\uDD47 ${discord.asMention}: ${database.balance.format()}$"
                1 -> "\n${i + 1} \uD83E\uDD48 ${discord.asMention}:: ${database.balance.format()}\$"
                2 -> "\n${i + 1} \uD83E\uDD49 ${discord.asMention}:: ${database.balance.format()}\$"
                else -> "\n${i + 1} \uD83C\uDFC5 ${discord.asMention}:: ${database.balance.format()}\$"
            }
        }
        event.reply {
            text = leaderboard
            transform = false
        }.queue()
    }
}