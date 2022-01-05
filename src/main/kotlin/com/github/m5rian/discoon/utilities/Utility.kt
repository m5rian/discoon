package com.github.m5rian.discoon.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class GuildMember(val guildId: String, val userId: String)

val lifeCycleCoroutine = CoroutineScope(Dispatchers.Default)
val commandsCoroutine = CoroutineScope(Dispatchers.Default)
val listenerCoroutine = CoroutineScope(Dispatchers.Default)
fun Double.format(): String = "%.2f".format(this)

val Int.inMinutes: Long get() = this * 1000L * 60L