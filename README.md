# Post Office Simulation

## Overview
A multi-threaded Java program simulating a post office with multiple workers serving customers using semaphores for synchronization. The simulation includes customer capacity limits, varied services, and resource management.

## System Components

### Constants
- `NUM_CUSTOMERS`: 50 customers total
- `NUM_WORKERS`: 3 postal workers
- `OFFICE_LIMIT`: 10 customers maximum in post office at once

### Services
1. **Buy Stamps**
   - Processing time: 1 second
   - No special resources needed

2. **Mail Letter**
   - Processing time: 1.5 seconds
   - No special resources needed

3. **Mail Package**
   - Processing time: 2 seconds
   - Requires exclusive access to scale
   - Scale is released after use

## Implementation Details

### Thread Classes

#### Customer Thread
- Represents individual customers entering the post office
- Workflow:
  1. Attempts to enter post office (if under capacity)
  2. Gets assigned random task
  3. Requests available worker
  4. Makes service request
  5. Waits for service completion
  6. Exits post office

#### Worker Thread
- Represents postal workers serving customers
- Workflow:
  1. Signals availability for new customer
  2. Accepts customer request
  3. Processes requested service
  4. Signals task completion
  5. Returns to available state

### Synchronization Mechanism

#### Primary Semaphores
- `postOfficeLimit`: Controls customer entry (max 10)
- `workerAvailable`: Signals worker availability
- `scale`: Manages exclusive access to package scale
- `worker[]`: Individual worker availability
- `customerInfo[]`: Customer information transfer
- `taskCompletion[]`: Service completion signals
- `serving[]`: Service initiation signals

#### Protection Semaphores
- `oneCustomer`: Ensures sequential customer requests
- `oneWorker`: Controls worker assignment
- `taskCompleted`: Protects customer count updates

### Task Processing
1. **Customer Assignment**
   - Random task generation
   - Worker availability check
   - Task information transfer

2. **Service Execution**
   - Task-specific processing times
   - Resource acquisition (scale for packages)
   - Completion notification

3. **Completion Handling**
   - Task completion signal
   - Customer count update
   - Resource release

## Task Timing
- Stamps: 1 second
- Letters: 1.5 seconds
- Packages: 2 seconds (plus scale access)

## Output Tracking
The simulation provides real-time status updates for:
- Customer/worker creation
- Post office entry/exit
- Service requests
- Service completion
- Scale usage
