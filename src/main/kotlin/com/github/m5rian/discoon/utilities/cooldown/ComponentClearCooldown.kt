package com.github.m5rian.discoon.utilities.cooldown

import com.github.m5rian.discoon.utilities.deleteComponents
import net.dv8tion.jda.api.entities.Message

/**
 * Cooldown implementation which removes the interaction components of a message.
 */
object ComponentClearCooldown : Cooldown {
    override fun onCooldownExpire(message: Message) {
        message.deleteComponents()
    }
}