package com.github.m5rian.discoon.enteties.workers

data class WorkerTier(val tier: Short, val price: Double)

val workerTiers: List<WorkerTier> = listOf(
    WorkerTier(1,499.99),
    WorkerTier(2,1299.99),
    WorkerTier(3,1999.99),
    WorkerTier(4,2499.99),
    WorkerTier(5,3299.99)
)