package com.example.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class TestCommand(
    @TargetAggregateIdentifier
    val id: String,
    val data: String
)