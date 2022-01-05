package com.github.m5rian.discoon.utilities

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction

data class EmbedDesigner(
        var text: String? = null
)

fun Interaction.reply(ephemeral: Boolean = false, embed: EmbedDesigner.() -> Unit): ReplyAction {
    val e = EmbedDesigner().apply(embed)

    return this.replyEmbeds(EmbedBuilder()
        .setDescription(e.text)
        .setFooter(this.member?.effectiveName, this.member?.effectiveAvatarUrl)
        .setColor(0x36393f)
        .build())
        .setEphemeral(ephemeral)
}

fun ComponentInteraction.edit(embed: EmbedDesigner.() -> Unit): ReplyAction? {
    val e = EmbedDesigner().apply(embed)

    return null
}

fun ComponentInteraction.addToast(message: String): UpdateInteractionAction {
    val originalEmbed = this@addToast.message.embeds[0]
    val embed = EmbedBuilder(this.message.embeds[0]).apply {
        appendDescription("\n\n> $message")
    }

    commandsCoroutine.launch {
        delay(5000)
        this@addToast.hook.editOriginalEmbeds(originalEmbed).queue()
    }
    return this.editMessageEmbeds(embed.build())
}