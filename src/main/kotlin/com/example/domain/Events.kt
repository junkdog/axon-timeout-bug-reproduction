package com.example.domain

import org.axonframework.serialization.Revision

@Revision("1.0")
data class TestEvent(
    val id: String,
    val data: String
)