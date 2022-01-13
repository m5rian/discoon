package com.github.m5rian.discoon.utilities

import com.github.m5rian.kotlingua.ArgumentBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction
import java.awt.Color

data class EmbedDesigner(
        var text: String? = null,
        var colour: String = "#2F3136",
        var components: List<ActionRow> = emptyList(),
        var variables: suspend ArgumentBuilder.() -> Unit = {},
        var transform: Boolean = true,
)

suspend fun Interaction.reply(ephemeral: Boolean = false, embed: suspend EmbedDesigner.() -> Unit): ReplyAction {
    val e = EmbedDesigner().also { embed.invoke(it) }
    val text = if (e.transform) lang.get(e.text!!, e.variables) else e.text!!

    return this.replyEmbeds(EmbedBuilder()
        .setDescription(text)
        .setFooter(this.member?.effectiveName, this.member?.effectiveAvatarUrl)
        .setColor(Color.decode(e.colour))
        .build())
        .setEphemeral(ephemeral)
        .addActionRows(e.components)
}

suspend fun ComponentInteraction.addToast(message: String, variables: suspend ArgumentBuilder.() -> Unit = {}, transform: Boolean = true): UpdateInteractionAction {
    val text = if (transform) lang.get(message, variables) else message
    val originalEmbed = this@addToast.message.embeds[0]
    val embed = EmbedBuilder(this.message.embeds[0]).apply {
        appendDescription("\n\n> $text")
    }

    commandsCoroutine.launch {
        delay(5000)
        this@addToast.hook.editOriginalEmbeds(originalEmbed).queue()
    }
    return this.editMessageEmbeds(embed.build())
}