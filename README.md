# Axon Framework Timeout Bug Reproduction

This project reproduces a potential bug where TrackingEventProcessor projections do not restart after being interrupted by Axon's janitor timeout mechanism.

## Problem Description

When a TrackingEventProcessor thread is interrupted due to timeout (via the janitor thread), the projection may not automatically restart to continue processing events, leading to event processing stalls.

## Project Structure

- **Java 21** with **Kotlin 2.2** and **Spring Boot 3.5.3**  
- **Axon Framework 4.11.2** (without Axon Server)
- **H2 in-memory database**
- **Maven build system**

### Key Components

1. **TestAggregate** - Simple aggregate that emits TestEvent
2. **TestProjection** - Tracking projection that simulates timeout-provoking behavior
3. **Timeout Configuration** - Aggressive timeout settings to trigger janitor interruptions

## Timeout Configuration

```yaml
axon:
  timeout:
    enabled: true
    transaction:
      event-processor:
        test-projection:
          timeout-ms: 5000          # 5 seconds - triggers timeout  
          warning-threshold-ms: 2000 # 2 seconds
          warning-interval-ms: 1000  # 1 second
```

## Reproduction Scenario

The `TestProjection` simulates problematic behavior:

- **Every 2nd event** sleeps for 15 seconds (exceeds 5-second timeout)
- **Other events** process immediately 
- Timeout should interrupt the 15-second sleep
- Projection should restart and continue processing

## Running the Reproduction

### Option 1: Automated Test

```bash
mvn test -Dtest=TimeoutBugReproductionTest
```

The test:
1. Sends 3 events (event #2 will timeout)  
2. Checks that all events are eventually processed

## Expected Behavior vs Bug

**Expected:**
1. Event #2 times out after 5 seconds
2. Janitor interrupts the projection thread  
3. TrackingEventProcessor automatically restarts
4. Event #3 is processed normally
5. Event #2 is eventually reprocessed

**Bug (confirmed):**
1. Event #2 times out after 5 seconds
2. Janitor interrupts the projection thread
3. **TrackingEventProcessor does NOT restart**
4. Event #3 is never processed
5. Processing stalls permanently

## Monitoring

Watch the logs for:

```
# Normal processing
INFO  - Processing TestEvent #1 for id: test-1
INFO  - Successfully processed TestEvent #1 for id: test-1

# Timeout warning  
WARN  - Event #2 will cause timeout - sleeping for 15 seconds
WARN  - UnitOfWork of EventProcessor test-projection is taking a long time...

# Janitor interruption
ERROR - UnitOfWork of EventProcessor test-projection has exceeded its timeout...
ERROR - Event processing was interrupted for event #2

# Final test results - Event #3 NEVER gets processed
INFO  - Processed events: 2, Items in projection: 1
WARN  - Timeout waiting for projection to process 3 items. Current count: 1
ERROR - All items should be in projection (found 1)
```

## Key Files

- `TestProjection.kt` - Contains timeout-provoking behavior
- `application.yml` - Aggressive timeout configuration  
- `TimeoutBugReproductionTest.kt` - Automated reproduction test

## Dependencies

This project uses only:
- Java 21
- Axon Framework 4.11.2 (spring-boot-starter, excluding axon-server-connector)
- Spring Boot 3.5.3
- Kotlin 2.2.0  
- H2 Database
- No external Axon Server required

The POM is fully self-contained and can be shared independently.