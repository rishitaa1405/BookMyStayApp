// Version 8.1 - Booking History & Reporting

import java.util.*;

// -------------------- ROOM --------------------
abstract class Room {
    private String roomType;
    private int beds;
    private double price;

    public Room(String type, int beds, double price) {
        this.roomType = type;
        this.beds = beds;
        this.price = price;
    }

    public String getRoomType() { return roomType; }
    public double getPrice() { return price; }
}

// -------------------- INVENTORY --------------------
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addRoom(String type, int count) {
        inventory.put(type, count);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void reduceRoom(String type) {
        inventory.put(type, getAvailability(type) - 1);
    }
}

// -------------------- RESERVATION --------------------
class Reservation {
    private String guestName;
    private String roomType;
    private String reservationId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void setReservationId(String id) {
        this.reservationId = id;
    }

    public String getReservationId() {
        return reservationId;
    }
}

// -------------------- QUEUE --------------------
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation getNext() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

// -------------------- 🔥 NEW: BOOKING HISTORY --------------------
class BookingHistory {

    private List<Reservation> history = new ArrayList<>();

    // Store confirmed booking
    public void addBooking(Reservation reservation) {
        history.add(reservation);
    }

    // Retrieve all bookings
    public List<Reservation> getAllBookings() {
        return Collections.unmodifiableList(history);
    }
}

// -------------------- 🔥 UPDATED: BOOKING SERVICE --------------------
class BookingService {

    private int counter = 1;

    public void processBookings(BookingRequestQueue queue,
                                RoomInventory inventory,
                                BookingHistory history) {

        System.out.println("\n=== Booking Confirmation ===\n");

        while (!queue.isEmpty()) {

            Reservation req = queue.getNext();
            String type = req.getRoomType();

            if (inventory.getAvailability(type) > 0) {

                String resId = type.replace(" ", "") + "-" + counter++;
                req.setReservationId(resId);

                inventory.reduceRoom(type);

                // 🔥 Store in history
                history.addBooking(req);

                System.out.println("CONFIRMED: " + req.getGuestName()
                        + " → " + resId);

            } else {
                System.out.println("FAILED: " + req.getGuestName());
            }
        }
    }
}

// -------------------- 🔥 NEW: REPORT SERVICE --------------------
class BookingReportService {

    // Display all bookings
    public void displayAllBookings(BookingHistory history) {

        System.out.println("\n=== Booking History ===");

        for (Reservation r : history.getAllBookings()) {
            System.out.println(r.getReservationId() +
                    " | " + r.getGuestName() +
                    " | " + r.getRoomType());
        }
    }

    // Summary report
    public void generateSummary(BookingHistory history) {

        Map<String, Integer> summary = new HashMap<>();

        for (Reservation r : history.getAllBookings()) {
            summary.put(r.getRoomType(),
                    summary.getOrDefault(r.getRoomType(), 0) + 1);
        }

        System.out.println("\n=== Booking Summary Report ===");

        for (String type : summary.keySet()) {
            System.out.println(type + " → " + summary.get(type) + " bookings");
        }
    }
}

// -------------------- MAIN CLASS (UNCHANGED) --------------------
public class BookMyStayApp {

    public static void main(String[] args) {

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 2);
        inventory.addRoom("Double Room", 1);

        // Queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room"));

        // 🔥 Booking History
        BookingHistory history = new BookingHistory();

        // Booking Process
        BookingService service = new BookingService();
        service.processBookings(queue, inventory, history);

        // 🔥 Reporting
        BookingReportService report = new BookingReportService();

        report.displayAllBookings(history);
        report.generateSummary(history);

        System.out.println("\nApplication Terminated");
    }
}