package com.brigadeiro.app.domain

fun interface TimeProvider {
    fun now(): Long
}
