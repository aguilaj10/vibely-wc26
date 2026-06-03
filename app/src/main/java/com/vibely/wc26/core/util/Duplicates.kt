package com.vibely.wc26.core.util

/**
 * Duplicate / ownership math, centralized so every consumer agrees:
 *   quantity 0 = missing, 1 = owned, 2+ = duplicated.
 *   Duplicates count == quantity - 1 (floored at 0).
 */

fun isOwned(quantity: Int): Boolean = quantity >= 1

fun isDuplicated(quantity: Int): Boolean = quantity >= 2

fun duplicatesOf(quantity: Int): Int = (quantity - 1).coerceAtLeast(0)
