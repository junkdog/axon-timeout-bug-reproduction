package com.example.projection

import com.example.domain.TestEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
@ProcessingGroup("test-projection")
class TestProjection {
    
    private val logger = LoggerFactory.getLogger(TestProjection::class.java)
    private val processedEvents = AtomicInteger(0)
    private val items = ConcurrentHashMap<String, String>()
    
    @EventHandler
    fun on(event: TestEvent) {
        val eventNumber = processedEvents.incrementAndGet()
        logger.info("Processing TestEvent #{} for id: {}", eventNumber, event.id)
        
        // Simulate timeout-provoking behavior: every 2nd event takes a long time
        if (eventNumber % 2 == 0) {
            logger.warn("Event #{} will cause timeout - sleeping for 15 seconds", eventNumber)
            try {
                Thread.sleep(15_000) // 15 seconds - should trigger timeout
            } catch (e: InterruptedException) {
                logger.error("Event processing was interrupted for event #{}", eventNumber, e)
                Thread.currentThread().interrupt()
                throw e // Re-throw to ensure Axon sees the interruption
            }
        }
        
        items[event.id] = event.data
        logger.info("Successfully processed TestEvent #{} for id: {}", eventNumber, event.id)
    }
    
    fun getProcessedEventCount(): Int = processedEvents.get()
    
    fun getItem(id: String): String? = items[id]
    
    fun getItemCount(): Int = items.size
}