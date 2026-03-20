// Version 11.1 - Concurrent Booking Simulation (Thread Safety)

import java.util.*;

// -------------------- INVENTORY (THREAD SAFE) --------------------
class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    public synchronized void addRoom(String type, int count) {
        inventory.put(type, count);
    }

    public synchronized int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    // 🔥 CRITICAL SECTION (synchronized)
    public synchronized boolean allocateRoom(String type) {
        int available = getAvailability(type);

        if (available > 0) {
            inventory.put(type, available - 1);
            return true;
        }
        return false;
    }
}

// -------------------- RESERVATION --------------------
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

// -------------------- THREAD-SAFE QUEUE --------------------
class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    // 🔥 synchronized enqueue
    public synchronized void addRequest(Reservation r) {
        queue.offer(r);
    }

    // 🔥 synchronized dequeue
    public synchronized Reservation getNext() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

// -------------------- 🔥 CONCURRENT BOOKING PROCESSOR --------------------
class BookingProcessor implements Runnable {

    private BookingRequestQueue queue;
    private RoomInventory inventory;
    private static int counter = 1;

    public BookingProcessor(BookingRequestQueue queue, RoomInventory inventory) {
        this.queue = queue;
        this.inventory = inventory;
    }

    @Override
    public void run() {

        while (true) {

            Reservation request;

            // 🔥 Critical section for queue access
            synchronized (queue) {
                if (queue.isEmpty()) break;
                request = queue.getNext();
            }

            if (request == null) continue;

            // 🔥 Critical section for allocation
            synchronized (inventory) {

                boolean allocated = inventory.allocateRoom(request.getRoomType());

                if (allocated) {
                    String roomId = request.getRoomType().replace(" ", "") + "-" + counter++;

                    System.out.println(Thread.currentThread().getName()
                            + " → CONFIRMED: " + request.getGuestName()
                            + " | " + roomId);
                } else {
                    System.out.println(Thread.currentThread().getName()
                            + " → FAILED: " + request.getGuestName()
                            + " (No rooms)");
                }
            }
        }
    }
}

// -------------------- MAIN CLASS --------------------
public class BookMyStayApp {

    public static void main(String[] args) {

        // Shared Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 2);

        // Shared Queue
        BookingRequestQueue queue = new BookingRequestQueue();

        // Simulate multiple users
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Single Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room"));
        queue.addRequest(new Reservation("David", "Single Room"));

        // 🔥 Multiple threads (guests booking simultaneously)
        Thread t1 = new Thread(new BookingProcessor(queue, inventory), "Thread-1");
        Thread t2 = new Thread(new BookingProcessor(queue, inventory), "Thread-2");
        Thread t3 = new Thread(new BookingProcessor(queue, inventory), "Thread-3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nFinal Available Rooms: "
                + inventory.getAvailability("Single Room"));

        System.out.println("\nApplication Terminated");
    }
}