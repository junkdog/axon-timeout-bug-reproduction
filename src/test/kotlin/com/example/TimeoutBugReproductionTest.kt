package com.example

import com.example.domain.TestCommand
import com.example.projection.TestProjection
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class TimeoutBugReproductionTest {
    
    private val logger = LoggerFactory.getLogger(TimeoutBugReproductionTest::class.java)
    
    @Autowired
    private lateinit var commandGateway: CommandGateway
    
    @Autowired
    private lateinit var testProjection: TestProjection
    
    @Test
    fun `should restart projection after janitor timeout interruption`() {
        logger.info("Starting timeout bug reproduction test")
        
        // Event 1: Normal processing
        commandGateway.send<Void>(TestCommand("test-1", "data-1"))
        
        // Event 2: This will timeout (15 seconds, but timeout is 5 seconds)
        logger.info("Sending event that will cause timeout...")
        commandGateway.send<Void>(TestCommand("test-2", "data-2"))
        
        // Wait for timeout to occur
        logger.info("Waiting for timeout to occur...")
        Thread.sleep(8_000) // 8 seconds - longer than 5 second timeout
        
        // Event 3: Should process normally if restart works
        logger.info("Sending event after timeout to test restart...")
        commandGateway.send<Void>(TestCommand("test-3", "data-3"))
        
        // Wait for processing to complete
        logger.info("Waiting for all events to be processed...")
        waitForProjectionToProcess(3, 20_000) // Wait up to 20 seconds
        
        // Verify all events were eventually processed
        val processedCount = testProjection.getProcessedEventCount()
        val itemCount = testProjection.getItemCount()
        
        logger.info("Processed events: {}, Items in projection: {}", processedCount, itemCount)
        
        // All 3 events should eventually be processed
        assertTrue(itemCount >= 3, "All items should be in projection (found $itemCount)")
        
        // Check specific items exist
        assertTrue(testProjection.getItem("test-1") != null, "Item 1 should exist")
        assertTrue(testProjection.getItem("test-2") != null, "Item 2 should exist (despite timeout)")
        assertTrue(testProjection.getItem("test-3") != null, "Item 3 should exist (after restart)")
        
        logger.info("Test completed successfully - projection restarted after timeout")
    }
    
    private fun waitForProjectionToProcess(expectedItemCount: Int, timeoutMs: Long) {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (testProjection.getItemCount() >= expectedItemCount) {
                logger.info("Projection has processed {} items", testProjection.getItemCount())
                return
            }
            
            Thread.sleep(1000)
            logger.info("Waiting for projection... Current item count: {}", testProjection.getItemCount())
        }
        
        logger.warn("Timeout waiting for projection to process {} items. Current count: {}", 
                   expectedItemCount, testProjection.getItemCount())
    }
}