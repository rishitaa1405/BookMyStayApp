// Version 5.1 - Booking Request Queue (FIFO)

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

// Inventory (UNCHANGED)
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

    public Map<String, Integer> getAllInventory() {
        return Collections.unmodifiableMap(inventory);
    }
}

// Search Service (UNCHANGED)
class RoomSearchService {

    public void searchAvailableRooms(List<Room> rooms, RoomInventory inventory) {

        System.out.println("\n=== Available Rooms ===\n");

        for (Room room : rooms) {
            int available = inventory.getAvailability(room.getRoomType());

            if (available > 0) {
                room.displayRoomDetails();
                System.out.println("Available: " + available);
                System.out.println("----------------------------------");
            }
        }
    }
}

// 🔥 NEW: Reservation (Booking Request)
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

    public void displayReservation() {
        System.out.println("Guest: " + guestName + " | Requested Room: " + roomType);
    }
}

// 🔥 NEW: Booking Queue (FIFO)
class BookingRequestQueue {

    private Queue<Reservation> queue;

    public BookingRequestQueue() {
        queue = new LinkedList<>();
    }

    // Add request (enqueue)
    public void addRequest(Reservation reservation) {
        queue.offer(reservation);
        System.out.println("Booking request added for " + reservation.getGuestName());
    }

    // View all requests
    public void displayQueue() {
        System.out.println("\n=== Booking Request Queue ===");

        for (Reservation r : queue) {
            r.displayReservation();
        }
    }
}

// Main Class (UNCHANGED)
public class BookMyStayApp {

    public static void main(String[] args) {

        // Room Objects
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        List<Room> rooms = Arrays.asList(single, doubleRoom, suite);

        // Inventory Setup
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 5);
        inventory.addRoom("Double Room", 2);
        inventory.addRoom("Suite Room", 1);

        // Search Available Rooms
        RoomSearchService searchService = new RoomSearchService();
        searchService.searchAvailableRooms(rooms, inventory);

        // 🔥 Booking Request Queue
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        // Guests submit requests (FIFO order)
        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Suite Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Double Room"));

        // Display queue
        bookingQueue.displayQueue();

        System.out.println("\n(No allocation done yet - Queue only)");
        System.out.println("Application Terminated");
    }
}