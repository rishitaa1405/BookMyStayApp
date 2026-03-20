// Version 9.1 - Error Handling & Validation

import java.util.*;

// -------------------- 🔥 CUSTOM EXCEPTIONS --------------------
class InvalidRoomTypeException extends Exception {
    public InvalidRoomTypeException(String message) {
        super(message);
    }
}

class NoAvailabilityException extends Exception {
    public NoAvailabilityException(String message) {
        super(message);
    }
}

// -------------------- ROOM --------------------
abstract class Room {
    private String roomType;

    public Room(String type) {
        this.roomType = type;
    }

    public String getRoomType() {
        return roomType;
    }
}

// -------------------- INVENTORY --------------------
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addRoom(String type, int count) {
        inventory.put(type, count);
    }

    public boolean isValidRoomType(String type) {
        return inventory.containsKey(type);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void reduceRoom(String type) throws NoAvailabilityException {
        int available = getAvailability(type);

        if (available <= 0) {
            throw new NoAvailabilityException("No rooms available for " + type);
        }

        inventory.put(type, available - 1);
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

// -------------------- 🔥 VALIDATOR --------------------
class BookingValidator {

    public static void validate(Reservation reservation,
                                RoomInventory inventory)
            throws InvalidRoomTypeException, NoAvailabilityException {

        String roomType = reservation.getRoomType();

        // Validate room type
        if (!inventory.isValidRoomType(roomType)) {
            throw new InvalidRoomTypeException(
                    "Invalid room type: " + roomType);
        }

        // Validate availability
        if (inventory.getAvailability(roomType) <= 0) {
            throw new NoAvailabilityException(
                    "No availability for room type: " + roomType);
        }
    }
}

// -------------------- BOOKING HISTORY --------------------
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public void addBooking(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllBookings() {
        return Collections.unmodifiableList(history);
    }
}

// -------------------- 🔥 BOOKING SERVICE --------------------
class BookingService {

    private int counter = 1;

    public void processBookings(BookingRequestQueue queue,
                                RoomInventory inventory,
                                BookingHistory history) {

        System.out.println("\n=== Booking Processing (Validated) ===\n");

        while (!queue.isEmpty()) {

            Reservation req = queue.getNext();

            try {
                // 🔥 VALIDATION (Fail Fast)
                BookingValidator.validate(req, inventory);

                // If valid → proceed
                String resId = req.getRoomType().replace(" ", "") + "-" + counter++;
                req.setReservationId(resId);

                inventory.reduceRoom(req.getRoomType());
                history.addBooking(req);

                System.out.println("CONFIRMED: " + req.getGuestName()
                        + " → " + resId);

            } catch (InvalidRoomTypeException | NoAvailabilityException e) {

                // 🔥 Graceful failure
                System.out.println("ERROR for " + req.getGuestName()
                        + ": " + e.getMessage());
            }
        }
    }
}

// -------------------- MAIN CLASS (UNCHANGED) --------------------
public class BookMyStayApp {

    public static void main(String[] args) {

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 1);
        inventory.addRoom("Double Room", 1);

        // Queue with VALID + INVALID inputs
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));   // valid
        queue.addRequest(new Reservation("Bob", "Suite Room"));     // invalid type
        queue.addRequest(new Reservation("Charlie", "Single Room")); // no availability

        // History
        BookingHistory history = new BookingHistory();

        // Process bookings
        BookingService service = new BookingService();
        service.processBookings(queue, inventory, history);

        System.out.println("\nApplication Terminated");
    }
}