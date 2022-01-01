package com.github.m5rian.discoon.enteties.managers

/**
 * Lists all manager tiers.
 *
 * The worker has a default price
 * The price of the worker gets calculated by multiplying the multiplier of the worker with the tier of the worker
 * to buy the manager on.
 *
 * @property price      The default price of the worker. You pay this as soon as you buy a new worker.
 * @property multiplier The number which gets multiplied with the tier of the worker you want to hire the manager on.
 *                      This result of this equation will be the price of hiring the manager.
 */
enum class ManagerTiers(val price: Double, val multiplier: Double) {

    ONE(2500.0, 2.5)

}