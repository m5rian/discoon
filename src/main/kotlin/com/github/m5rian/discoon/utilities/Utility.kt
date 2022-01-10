package com.github.m5rian.discoon.utilities

import com.github.m5rian.kotlingua.Kotlingua
import com.github.m5rian.kotlingua.Lang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

val Kotlingua.XMAS get() = Lang("xmas", "Christmas")

val lang: Lang
    get() {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val month = calendar.get(Calendar.MONTH)
        return if (month > 10 || month == 0) Kotlingua.XMAS
        else Kotlingua.defaultLang
    }

data class GuildMember(val guildId: String, val userId: String)

val lifeCycleCoroutine = CoroutineScope(Dispatchers.Default)
val commandsCoroutine = CoroutineScope(Dispatchers.Default)
val listenerCoroutine = CoroutineScope(Dispatchers.Default)
fun Double.format(): String = "%.2f".format(this)

val Int.inMinutes: Long get() = this * 1000L * 60L