package com.dlstudio.app.i18n

/** Русское склонение по числу: 1 колода, 2 колоды, 5 колод, 11 колод, 21 колода. */
fun ruPlural(n: Int, one: String, few: String, many: String): String = when {
    n % 100 in 11..14 -> many
    n % 10 == 1 -> one
    n % 10 in 2..4 -> few
    else -> many
}
