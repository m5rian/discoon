package com.github.m5rian.discoon.database

import com.github.m5rian.discoon.config
import com.github.m5rian.discoon.lifecycles.WorkerIncome
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Represents a single worker.
 *
 * @property tier Level of worker.
 */
data class Worker(
        val tier: Short
) {
    private var job: Job? = null

    /**
     * Starts earning money by working.
     * Income depends on the tier.
     */
    fun startIncome(player: Player) {
        this.job = WorkerIncome.workScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(1)) // Earn money every minute

                val income = config.baseIncome * this@Worker.tier
                player.addBalance(income)
            }
        }
    }

    /**
     * Stops earning money.
     *
     */
    fun stopIncome() = job?.cancel()

}