// Version 10.1 - Booking Cancellation & Inventory Rollback

import java.util.*;

// -------------------- CUSTOM EXCEPTIONS --------------------
class InvalidRoomTypeException extends Exception {
    public InvalidRoomTypeException(String msg) { super(msg); }
}

class NoAvailabilityException extends Exception {
    public NoAvailabilityException(String msg) { super(msg); }
}

class InvalidCancellationException extends Exception {
    public InvalidCancellationException(String msg) { super(msg); }
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
        if (getAvailability(type) <= 0)
            throw new NoAvailabilityException("No rooms available for " + type);

        inventory.put(type, getAvailability(type) - 1);
    }

    // 🔥 NEW: rollback increment
    public void increaseRoom(String type) {
        inventory.put(type, getAvailability(type) + 1);
    }
}

// -------------------- RESERVATION --------------------
class Reservation {
    private String guestName;
    private String roomType;
    private String reservationId;
    private boolean isCancelled = false;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void setReservationId(String id) { this.reservationId = id; }
    public String getReservationId() { return reservationId; }

    public boolean isCancelled() { return isCancelled; }
    public void cancel() { this.isCancelled = true; }
}

// -------------------- QUEUE --------------------
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) { queue.offer(r); }
    public Reservation getNext() { return queue.poll(); }
    public boolean isEmpty() { return queue.isEmpty(); }
}

// -------------------- BOOKING HISTORY --------------------
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public void addBooking(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllBookings() {
        return history;
    }

    public Reservation findById(String id) {
        for (Reservation r : history) {
            if (id.equals(r.getReservationId())) {
                return r;
            }
        }
        return null;
    }
}

// -------------------- BOOKING SERVICE --------------------
class BookingService {

    private int counter = 1;

    public void processBookings(BookingRequestQueue queue,
                                RoomInventory inventory,
                                BookingHistory history) {

        while (!queue.isEmpty()) {
            Reservation req = queue.getNext();

            try {
                if (!inventory.isValidRoomType(req.getRoomType())) {
                    throw new InvalidRoomTypeException("Invalid room type");
                }

                String resId = req.getRoomType().replace(" ", "") + "-" + counter++;

                inventory.reduceRoom(req.getRoomType());

                req.setReservationId(resId);
                history.addBooking(req);

                System.out.println("CONFIRMED: " + req.getGuestName() + " → " + resId);

            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }
}

// -------------------- 🔥 NEW: CANCELLATION SERVICE --------------------
class CancellationService {

    private Stack<String> rollbackStack = new Stack<>();

    public void cancelBooking(String reservationId,
                              BookingHistory history,
                              RoomInventory inventory)
            throws InvalidCancellationException {

        Reservation reservation = history.findById(reservationId);

        // Validation
        if (reservation == null) {
            throw new InvalidCancellationException("Reservation not found");
        }

        if (reservation.isCancelled()) {
            throw new InvalidCancellationException("Already cancelled");
        }

        // 🔥 LIFO rollback tracking
        rollbackStack.push(reservationId);

        // Restore inventory
        inventory.increaseRoom(reservation.getRoomType());

        // Mark cancelled
        reservation.cancel();

        System.out.println("CANCELLED: " + reservationId +
                " (Room restored)");
    }

    public void showRollbackStack() {
        System.out.println("\nRollback Stack: " + rollbackStack);
    }
}

// -------------------- MAIN CLASS --------------------
public class BookMyStayApp {

    public static void main(String[] args) {

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 2);

        // Queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Single Room"));

        // History
        BookingHistory history = new BookingHistory();

        // Booking
        BookingService bookingService = new BookingService();
        bookingService.processBookings(queue, inventory, history);

        // 🔥 Cancellation
        CancellationService cancelService = new CancellationService();

        try {
            cancelService.cancelBooking("SingleRoom-1", history, inventory);
            cancelService.cancelBooking("SingleRoom-2", history, inventory);

            // Invalid cancellation
            cancelService.cancelBooking("SingleRoom-1", history, inventory);

        } catch (InvalidCancellationException e) {
            System.out.println("CANCELLATION ERROR: " + e.getMessage());
        }

        cancelService.showRollbackStack();

        System.out.println("\nAvailable Rooms after rollback: "
                + inventory.getAvailability("Single Room"));

        System.out.println("\nApplication Terminated");
    }
}