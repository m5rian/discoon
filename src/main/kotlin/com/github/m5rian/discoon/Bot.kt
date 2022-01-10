package com.github.m5rian.discoon

import com.github.m5rian.discoon.commands.CommandManager
import com.github.m5rian.discoon.database.database
import com.github.m5rian.discoon.lifecycles.WorkerIncome
import com.github.m5rian.discoon.utilities.ButtonManager
import com.github.m5rian.discoon.utilities.Resource
import com.github.m5rian.discoon.utilities.SelectionMenuManager
import com.github.m5rian.discoon.utilities.XMAS
import com.github.m5rian.kotlingua.Kotlingua
import com.github.m5rian.kotlingua.Lang
import com.github.m5rian.kotlingua.kotlingua
import dev.minn.jda.ktx.injectKTX
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import kotlin.time.ExperimentalTime

val config: Config = Json.decodeFromString(Resource.loadString("config.json"))

@ExperimentalTime
fun main() {
    kotlingua {
        directory = "languages/"
        defaultLang = ENGLISH_UNITED_KINGDOM
        loadCustomLanguage(Kotlingua.XMAS)
    }.loadLanguages()

    database {
        connectionString = config.databaseConnection
        database = "Discoon"
    }.connect()

    JDABuilder.createDefault(config.token)
        .injectKTX()
        .addEventListeners(CommandManager, SelectionMenuManager, ButtonManager, WorkerIncome)
        .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
        .enableCache(CacheFlag.ONLINE_STATUS)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .build()
}
