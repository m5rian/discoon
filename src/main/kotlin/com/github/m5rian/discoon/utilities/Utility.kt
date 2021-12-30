package com.github.m5rian.discoon.utilities

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

val lifeCycleCoroutine = CoroutineScope(Dispatchers.Default)
fun Double.format(): String = "%.2f".format(this)