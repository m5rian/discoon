package com.github.m5rian.discoon.utilities

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction


data class EmbedDesigner(
        var text: String? = null
)

fun Interaction.reply(embed: EmbedDesigner.() -> Unit): ReplyAction {
    val e = EmbedDesigner().apply(embed)

    return this.replyEmbeds(EmbedBuilder()
        .setDescription(e.text)
        .setFooter(this.member?.effectiveName, this.member?.effectiveAvatarUrl)
        .setColor(0x36393f)
        .build())
}