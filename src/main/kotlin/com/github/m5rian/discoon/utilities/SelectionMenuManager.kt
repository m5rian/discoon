package com.github.m5rian.discoon.utilities

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

object SelectionMenuManager : ListenerAdapter() {

    data class Data(val user: User, val callback: (event: SelectionMenuEvent) -> Unit)

    private val ids: MutableList<Int> = mutableListOf()
    fun generateId(): Int {
        ids.forEachIndexed { i, id -> if (i != id) return i.also { ids.add(i, i) } }
        return ids.size.also { ids.add(ids.size) }
    }

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
