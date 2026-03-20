// Version 12.1 - Data Persistence & System Recovery

import java.io.*;
import java.util.*;

// -------------------- INVENTORY --------------------
class RoomInventory implements Serializable {

    private static final long serialVersionUID = 1L;
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

    public Map<String, Integer> getInventory() {
        return inventory;
    }
}

// -------------------- RESERVATION --------------------
class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

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

// -------------------- BOOKING HISTORY --------------------
class BookingHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Reservation> history = new ArrayList<>();

    public void addBooking(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllBookings() {
        return history;
    }
}

// -------------------- 🔥 PERSISTENCE SERVICE --------------------
class PersistenceService {

    private static final String FILE_NAME = "hotel_state.ser";

    // Save state
    public void save(RoomInventory inventory, BookingHistory history) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(inventory);
            out.writeObject(history);

            System.out.println("💾 State saved successfully.");

        } catch (IOException e) {
            System.out.println("ERROR saving state: " + e.getMessage());
        }
    }

    // Load state
    public Object[] load() {

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            RoomInventory inventory = (RoomInventory) in.readObject();
            BookingHistory history = (BookingHistory) in.readObject();

            System.out.println("🔄 State restored successfully.");

            return new Object[]{inventory, history};

        } catch (FileNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("ERROR loading state: " + e.getMessage());
        }

        return null;
    }
}

// -------------------- BOOKING SERVICE --------------------
class BookingService {

    private int counter = 1;

    public void book(String guest, String type,
                     RoomInventory inventory,
                     BookingHistory history) {

        if (inventory.getAvailability(type) > 0) {

            String id = type.replace(" ", "") + "-" + counter++;
            Reservation r = new Reservation(guest, type);
            r.setReservationId(id);

            inventory.reduceRoom(type);
            history.addBooking(r);

            System.out.println("CONFIRMED: " + guest + " → " + id);

        } else {
            System.out.println("FAILED: " + guest + " (No rooms)");
        }
    }
}

// -------------------- MAIN CLASS --------------------
public class BookMyStayApp {

    public static void main(String[] args) {

        PersistenceService persistence = new PersistenceService();

        RoomInventory inventory;
        BookingHistory history;

        // 🔄 Try loading previous state
        Object[] state = persistence.load();

        if (state != null) {
            inventory = (RoomInventory) state[0];
            history = (BookingHistory) state[1];
        } else {
            // Fresh start
            inventory = new RoomInventory();
            history = new BookingHistory();

            inventory.addRoom("Single Room", 2);
            inventory.addRoom("Double Room", 1);
        }

        // Booking operations
        BookingService service = new BookingService();

        service.book("Alice", "Single Room", inventory, history);
        service.book("Bob", "Single Room", inventory, history);

        // Show history
        System.out.println("\n=== Booking History ===");
        for (Reservation r : history.getAllBookings()) {
            System.out.println(r.getReservationId() + " | " + r.getGuestName());
        }

        // 💾 Save before shutdown
        persistence.save(inventory, history);

        System.out.println("\nApplication Terminated");
    }
}