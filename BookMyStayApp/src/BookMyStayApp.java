// Version 7.1 - Add-On Service Selection

import java.util.*;

// -------------------- ROOM CLASSES --------------------
abstract class Room {
    private String roomType;
    private int numberOfBeds;
    private double pricePerNight;

    public Room(String roomType, int numberOfBeds, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() { return roomType; }
    public int getNumberOfBeds() { return numberOfBeds; }
    public double getPricePerNight() { return pricePerNight; }

    public abstract void displayRoomDetails();
}

class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 1, 1000.0); }
    public void displayRoomDetails() {
        System.out.println(getRoomType() + " ₹" + getPricePerNight());
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 2, 1800.0); }
    public void displayRoomDetails() {
        System.out.println(getRoomType() + " ₹" + getPricePerNight());
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 3, 3000.0); }
    public void displayRoomDetails() {
        System.out.println(getRoomType() + " ₹" + getPricePerNight());
    }
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

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
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

// -------------------- BOOKING SERVICE --------------------
class BookingService {

    private Set<String> allocatedRoomIds = new HashSet<>();
    private Map<String, Set<String>> roomAllocations = new HashMap<>();
    private int counter = 1;

    // 🔥 Return reservationId for add-on usage
    public List<String> processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        List<String> confirmedReservationIds = new ArrayList<>();

        System.out.println("\n=== Booking Confirmation ===\n");

        while (!queue.isEmpty()) {

            Reservation req = queue.getNext();
            String type = req.getRoomType();

            if (inventory.getAvailability(type) > 0) {

                String roomId = type.replace(" ", "") + "-" + counter++;

                allocatedRoomIds.add(roomId);

                roomAllocations.putIfAbsent(type, new HashSet<>());
                roomAllocations.get(type).add(roomId);

                inventory.reduceRoom(type);

                // 🔥 Reservation ID = Room ID (for simplicity)
                confirmedReservationIds.add(roomId);

                System.out.println("CONFIRMED: " + req.getGuestName() +
                        " → " + roomId);
            } else {
                System.out.println("FAILED: " + req.getGuestName() +
                        " → No rooms");
            }
        }

        return confirmedReservationIds;
    }
}

// -------------------- 🔥 NEW: ADD-ON SERVICE --------------------
class AddOnService {
    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() { return serviceName; }
    public double getCost() { return cost; }
}

// -------------------- 🔥 NEW: SERVICE MANAGER --------------------
class AddOnServiceManager {

    private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

    // Add service to reservation
    public void addService(String reservationId, AddOnService service) {
        serviceMap.putIfAbsent(reservationId, new ArrayList<>());
        serviceMap.get(reservationId).add(service);

        System.out.println("Added " + service.getServiceName() +
                " to Reservation " + reservationId);
    }

    // Calculate total cost
    public double calculateTotalCost(String reservationId) {
        double total = 0;

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services != null) {
            for (AddOnService s : services) {
                total += s.getCost();
            }
        }

        return total;
    }

    // Display services
    public void displayServices(String reservationId) {
        System.out.println("\nServices for " + reservationId + ":");

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null) {
            System.out.println("No services selected.");
            return;
        }

        for (AddOnService s : services) {
            System.out.println("- " + s.getServiceName() + " ₹" + s.getCost());
        }

        System.out.println("Total Add-On Cost: ₹" + calculateTotalCost(reservationId));
    }
}

// -------------------- MAIN CLASS (UNCHANGED) --------------------
public class BookMyStayApp {

    public static void main(String[] args) {

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoom("Single Room", 2);

        // Queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Single Room"));

        // Booking
        BookingService bookingService = new BookingService();
        List<String> reservationIds =
                bookingService.processBookings(queue, inventory);

        // 🔥 Add-On Services
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Add services to first reservation
        String resId = reservationIds.get(0);

        serviceManager.addService(resId, new AddOnService("Breakfast", 200));
        serviceManager.addService(resId, new AddOnService("WiFi", 100));
        serviceManager.addService(resId, new AddOnService("Airport Pickup", 500));

        // Display
        serviceManager.displayServices(resId);

        System.out.println("\nApplication Terminated");
    }
}