// Shahrukh Showkath - sxs200232
// CS 4348.501 
// Project #2:  Threads and Semaphores

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Proj2 {

    // Initialize number of customers, number of customers who can enter the post
    // office, and number of workers
    public static final int NUM_CUSTOMERS = 50;
    public static final int OFFICE_LIMIT = 10;
    public static final int NUM_WORKERS = 3;

    // Initialize semaphores
    static Semaphore worker[] = { new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true) };
    static Semaphore workerAvailable = new Semaphore(0, true);
    static Semaphore postOfficeLimit = new Semaphore(OFFICE_LIMIT, true);
    static Semaphore customerInfo[] = { new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true) };
    static Semaphore request[] = { new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true) };
    static Semaphore taskCompletion[] = { new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true) };
    static Semaphore oneCustomer = new Semaphore(1, true);
    static Semaphore oneWorker = new Semaphore(1, true);
    static Semaphore serving[] = { new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true) };
    static Semaphore taskCompleted = new Semaphore(1, true);
    static Semaphore[] customer = new Semaphore[NUM_CUSTOMERS];
    static {
        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            customer[i] = new Semaphore(0);
        }
    }
    static Semaphore tasks[] = { new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true) };
    static Semaphore scale = new Semaphore(1, true);

    // Initialize customerCount to stop worker loop once all customers have been
    // served
    static int customerCount = 0;

    // Random object to randomly generate task for a customer to request
    static Random random = new Random();

    public static void main(String[] args) {

        // Initialize and start customer/worker threads
        Thread workers[] = new Thread[NUM_WORKERS];
        Thread customers[] = new Thread[NUM_CUSTOMERS];

        for (int i = 0; i < NUM_WORKERS; i++) {
            workers[i] = new Thread(new Worker(i));
        }

        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            customers[i] = new Thread(new Customer(i));
        }

        for (int i = 0; i < NUM_WORKERS; i++) {
            workers[i].start();
        }

        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            customers[i].start();
        }

        // Run until ever customer has been serviced and join them
        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            try {
                customers[i].join();
                System.out.println("Joined customer " + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Prevent possibly infinitely running threads
        for (int i = 0; i < NUM_WORKERS; i++) {
            workers[i].interrupt();
        }
    }

    // Customer class thread to represent customers entering the post office
    public static class Customer implements Runnable {

        // ID of the customer
        int id;

        public Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {

                System.out.println("Customer " + id + " created");

                // Randomly generate task for customer
                int taskNumber = random.nextInt(3);

                // Check to see if customer can enter the post office
                postOfficeLimit.acquire();
                System.out.println("Customer " + id + " enters post office");

                // Ensure that only one customer is asking for a specific worker
                oneCustomer.acquire();

                // Check if a worker is available.
                workerAvailable.acquire();

                // Determine which worker is available to serve
                int workerNum = -1;
                if (worker[0].tryAcquire()) {
                    workerNum = 0;
                } else if (worker[1].tryAcquire()) {
                    workerNum = 1;
                } else if (worker[2].tryAcquire()) {
                    workerNum = 2;
                }

                // Release customer task/ID information to worker and inform worker of release
                customer[id].release(1);
                tasks[taskNumber].release(1);
                customerInfo[workerNum].release(1);

                // Allow another customer to request a task to a different worker
                oneCustomer.release(1);

                // Wait for worker to begin serving
                serving[workerNum].acquire();

                // Determine which task the customer requested from a specific worker
                switch (taskNumber) {
                    case 0: {
                        System.out.println("Customer " + id + " asks postal worker " + workerNum + " to buy stamps");
                        break;
                    }
                    case 1: {
                        System.out.println("Customer " + id + " asks postal worker " + workerNum + " to mail a letter");
                        break;
                    }
                    case 2: {
                        System.out
                                .println("Customer " + id + " asks postal worker " + workerNum + " to mail a package");
                        break;
                    }
                    default: {
                        System.out.println("Error");
                        break;
                    }
                }

                // Inform worker that customer has requested a task to be done
                request[workerNum].release(1);

                // Wait for worker to complete task
                taskCompletion[workerNum].acquire();

                // Leave post office once served
                System.out.println("Customer " + id + " leaves the post office");
                postOfficeLimit.release(1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Worker class thread to represent workers serving customers in the post office
    public static class Worker implements Runnable {

        // Worker ID number
        int id;

        public Worker(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {

                System.out.println("Postal worker " + id + " created");

                // Keep checking for customers
                while (true) {

                    // Ensure that one only one worker is serving one customer
                    oneWorker.acquire();

                    // Tell customer that a worker is able to serve them and which one
                    worker[id].release(1);
                    workerAvailable.release(1);

                    // Wait for customer to release information
                    customerInfo[id].acquire();

                    // Determine if all customers have been served and exit loop if so
                    if (customerCount >= NUM_CUSTOMERS) {
                        oneWorker.release();
                        break;
                    }

                    // Determine which customer the worker is serving
                    int customerNum = -1;
                    for (int i = 0; i < NUM_CUSTOMERS; i++) {
                        if (customer[i].tryAcquire()) {
                            customerNum = i;
                            break;
                        }
                    }

                    // Determine which task the customer is requesting
                    int taskNumber = -1;
                    if (tasks[0].tryAcquire()) {
                        taskNumber = 0;
                    } else if (tasks[1].tryAcquire()) {
                        taskNumber = 1;
                    } else if (tasks[2].tryAcquire()) {
                        taskNumber = 2;
                    }

                    // Allow other workers to service another customer
                    oneWorker.release(1);

                    // Inform customer that a worker is now serving them
                    System.out.println("Postal worker " + id + " serving customer " + customerNum);
                    serving[id].release(1);

                    // Wait for customer to print that they requested a task
                    request[id].acquire();

                    // Perform the task the customer requested and release taskCompletion once
                    // finished
                    switch (taskNumber) {
                        case 0: {
                            Thread.sleep(1000);
                            System.out.println("Postal worker " + id + " finished serving customer " + customerNum);
                            taskCompletion[id].release(1);
                            break;
                        }
                        case 1: {
                            Thread.sleep(1500);
                            System.out.println("Postal worker " + id + " finished serving customer " + customerNum);
                            taskCompletion[id].release(1);
                            break;
                        }
                        case 2: {
                            scale.acquire();
                            System.out.println("Scales in use by postal worker " + id);
                            Thread.sleep(2000);
                            System.out.println("Postal worker " + id + " finished serving customer " + customerNum);
                            taskCompletion[id].release(1);
                            System.out.println("Scales released by postal worker " + id);
                            scale.release(1);
                            break;
                        }
                        default: {
                            System.out.println("Error");
                            break;
                        }
                    }

                    // Increment customerCount after taskcompletion - semaphore ensures only one
                    // worker is incrementing at a time
                    taskCompleted.acquire();
                    customerCount++;
                    taskCompleted.release();

                    // Determine if all customers have been served and exit loop if so
                    if (customerCount >= NUM_CUSTOMERS) {
                        customerInfo[0].release();
                        customerInfo[1].release();
                        customerInfo[2].release();
                        break;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
