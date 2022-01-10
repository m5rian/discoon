package com.github.m5rian.discoon.enteties.workers

data class WorkerTier(val tier: Short, val price: Double)

val workerTiers: List<WorkerTier> = listOf(
    WorkerTier(1,499.99),
    WorkerTier(2,1999.99),
    WorkerTier(3,4999.99),
    WorkerTier(4,19999.99),
    WorkerTier(5,34999.99),
    WorkerTier(6,49999.99),
    WorkerTier(7,49999.99),
    WorkerTier(8,74999.99),
    WorkerTier(9,99999.99),
    WorkerTier(10,149999.99),
    WorkerTier(11,199999.99),
    WorkerTier(12,349999.99),
    WorkerTier(13,499999.99),
    WorkerTier(14,749999.99),
    WorkerTier(15,999999.99),
    WorkerTier(16,1499999.99),
    WorkerTier(17,1999999.99),
    WorkerTier(18,3499999.99),
    WorkerTier(19,4999999.99),
    WorkerTier(20,7499999.99),
    WorkerTier(21,9999999.99),
)