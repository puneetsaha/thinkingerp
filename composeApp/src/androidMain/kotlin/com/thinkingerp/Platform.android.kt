package com.thinkingerp

import java.util.UUID

actual fun generateId(): String = UUID.randomUUID().toString()
actual fun currentMillis(): Long = System.currentTimeMillis()
