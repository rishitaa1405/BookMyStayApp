// Version 6.1 - Reservation Confirmation & Room Allocation

import java.util.*;

// Abstract Class
abstract class Room {
    private String roomType;
    private int numberOfBeds;
    private double pricePerNight;

    public Room(String roomType, int numberOfBeds, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public abstract void displayRoomDetails();
}

// Room Types
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 1000.0);
    }

    public void displayRoomDetails() {
        System.out.println("Room Type: " + getRoomType());
        System.out.println("Beds: " + getNumberOfBeds());
        System.out.println("Price: ₹" + getPricePerNight());
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 1800.0);
    }

    public void displayRoomDetails() {
        System.out.println("Room Type: " + getRoomType());
        System.out.println("Beds: " + getNumberOfBeds());
        System.out.println("Price: ₹" + getPricePerNight());
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 3000.0);
    }

    public void displayRoomDetails() {
        System.out.println("Room Type: " + getRoomType());
        System.out.println("Beds: " + getNumberOfBeds());
        System.out.println("Price: ₹" + getPricePerNight());
    }
}

// Inventory
class RoomInventory {

    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
    }

    public void addRoom(String roomType, int count) {
        inventory.put(roomType, count);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    // 🔥 NEW: decrement after allocation
    public void reduceRoom(String roomType) {
        int count = getAvailability(roomType);
        if (count > 0) {
            inventory.put(roomType, count - 1);
        }
    }
}

// Reservation
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

// Booking Queue
class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation reservation) {
        queue.offer(reservation);
    }

    public Reservation getNextRequest() {
        return queue.poll(); // FIFO
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

// 🔥 NEW: Booking Service (CORE LOGIC)
class BookingService {

    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> roomAllocations = new HashMap<>();
    private int roomCounter = 1;

    public void processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        System.out.println("\n=== Processing Bookings ===\n");

        while (!queue.isEmpty()) {

            Reservation request = queue.getNextRequest();
            String roomType = request.getRoomType();

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                // Generate unique room ID
                String roomId = roomType.replace(" ", "") + "-" + roomCounter++;

                // Ensure uniqueness (extra safety)
                if (!allocatedRoomIds.contains(roomId)) {
                    allocatedRoomIds.add(roomId);

                    // Map room type → allocated IDs
                    roomAllocations.putIfAbsent(roomType, new HashSet<>());
                    roomAllocations.get(roomType).add(roomId);

                    // 🔥 Update inventory immediately
                    inventory.reduceRoom(roomType);

                    System.out.println("Booking CONFIRMED for " + request.getGuestName());
                    System.out.println("Room Type: " + roomType);
                    System.out.println("Allocated Room ID: " + roomId);
                    System.out.println("----------------------------------");
                }

            } else {
                System.out.println("Booking FAILED for " + request.getGuestName());
                System.out.println("Room Type: " + roomType + " is NOT available");
                System.out.println("----------------------------------");
            }
        }
    }
}

// Main Class (UNCHANGED)
public class BookMyStayApp {

    public static void main(String[] args) {

        // Inventory Setup
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 2);
        inventory.addRoom("Double Room", 1);
        inventory.addRoom("Suite Room", 1);

        // Booking Queue
        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Single Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room")); // should fail
        queue.addRequest(new Reservation("David", "Suite Room"));

        // 🔥 Process Bookings
        BookingService bookingService = new BookingService();
        bookingService.processBookings(queue, inventory);

        System.out.println("\nApplication Terminated");
    }
}