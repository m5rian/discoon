package com.github.m5rian.discoon.utilities

import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

val ids: MutableList<Int> = mutableListOf()
fun generateComponentId(): Int {
    ids.forEachIndexed { i, id -> if (i != id) return i.also { ids.add(i, i) } }
    return ids.size.also { ids.add(ids.size) }
}

object SelectionMenuManager : ListenerAdapter() {
    data class Data(val user: User, val callback: (event: SelectionMenuEvent) -> Unit)

    val waitingMenus = mutableMapOf<Int, Data>()

    override fun onSelectionMenu(event: SelectionMenuEvent) {
        val data = waitingMenus[event.componentId.toInt()] ?: return
        if (data.user == event.user) {
            data.callback.invoke(event)
        }
    }
}

fun SelectionMenu.Builder.onClick(user: User, callback: (event: SelectionMenuEvent) -> Unit) {
    SelectionMenuManager.waitingMenus[this.id.toInt()] = SelectionMenuManager.Data(user, callback)
}


object ButtonManager : ListenerAdapter() {
    data class Data(val user: User, val callback: suspend (event: ButtonClickEvent) -> Unit)

    val waitingButtons = mutableMapOf<Int, Data>()

    override fun onButtonClick(event: ButtonClickEvent) {
        listenerCoroutine.launch {
            val data = waitingButtons[event.componentId.toInt()] ?: return@launch
            if (data.user == event.user) {
                data.callback.invoke(event)
            }
        }
    }
}

fun Button.onClick(user: User, callback: suspend (event: ButtonClickEvent) -> Unit): Button {
    this.id?.let {
        ButtonManager.waitingButtons[it.toInt()] = ButtonManager.Data(user, callback)
    }
    return this
}

fun ButtonClickEvent.disableButtons() {
    // Remove waiting buttons
    this.message.buttons
        .mapNotNull { it.id }
        .forEach {
            ids.remove(it.toInt())
            ButtonManager.waitingButtons.remove(it.toInt())
        }
    // Disable buttons
    this.message.editMessageComponents(ActionRow.of(this.message.buttons.map { it.asDisabled() })).queue()
}

fun Message.deleteComponents() {
    this.actionRows.forEach { actionRow ->
        actionRow.components.forEach { component ->
            component.id?.let {
                ids.remove(it.toInt())
                ButtonManager.waitingButtons.remove(it.toInt())
                SelectionMenuManager.waitingMenus.remove(it.toInt())
            }
        }
    }
    this.editMessageComponents().queue()
}