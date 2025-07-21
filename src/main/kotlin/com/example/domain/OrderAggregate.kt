package com.example.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class TestAggregate() {
    
    @AggregateIdentifier
    private lateinit var id: String
    private lateinit var data: String
    
    @CommandHandler
    constructor(command: TestCommand) : this() {
        apply(TestEvent(
            id = command.id,
            data = command.data
        ))
    }
    
    @EventSourcingHandler
    fun on(event: TestEvent) {
        this.id = event.id
        this.data = event.data
    }
}