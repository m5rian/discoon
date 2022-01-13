package com.github.m5rian.discoon.utilities.cooldown

import net.dv8tion.jda.api.entities.Message

interface Cooldown {
    fun onCooldownExpire(message: Message)
}