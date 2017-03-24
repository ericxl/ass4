import java.io.*;
import java.util.*;

public class Inventory implements Serializable {
    Map<String, Integer> inventory;
    Map<Integer, String> orders = new HashMap<>();
    Map<String, ArrayList<Integer>> user_orders = new HashMap<>();
    int orderid = 1;

    public Inventory(String inventoryFile) {
        this.inventory = new HashMap<>();
        this.orders = new HashMap<>();
        this.user_orders = new HashMap<>();

        Scanner sc;
        try {
            sc = new Scanner(new FileReader(inventoryFile));
        } catch (FileNotFoundException e) {
            System.out.println("FATAL ERROR: Inventory file not found.");

            System.exit(-1);
            return;
        }

        // Parse the inventory file
        while (sc.hasNextLine()) {
            String product = sc.nextLine();

            String[] tokens = product.split(" ");
            if (tokens.length != 2) {
                continue;
            }

            inventory.put(tokens[0], Integer.parseInt(tokens[1]));
        }
    }

    private Inventory(Map<String, Integer> inventory,
                  Map<Integer, String> orders,
                  Map<String, ArrayList<Integer>> userOrders,
                  int orderNumber) {
        this.inventory = inventory;
        this.orders = orders;
        this.user_orders = userOrders;
        this.orderid = orderNumber;
    }

    public synchronized String purchase(String user, String item, int want) {
        if(!inventory.keySet().contains(item)){
            return "Not Available - We do not sell this product";
        }
        Integer left = inventory.get(item);
        if(want > left){
            return "Not Available - Not enough items";
        }
        orders.put(new Integer(orderid), user + " " + item + " " + want);
        if(!user_orders.containsKey(user)){
            user_orders.put(user, new ArrayList<Integer>());
        }
        user_orders.get(user).add(new Integer(orderid));

        int current = left - want;
        inventory.put(item, new Integer(current));

        String response = "You order has been placed, " + orderid + " " + user + " " + item + " " + want;
        orderid++;
        return response;
    }

    public synchronized String cancel(int id) {
        if(!orders.containsKey(new Integer(id))) {
            return id + " not found, no such order";
        }
        String order = orders.get(new Integer(id));
        String[] orderToken = order.split(" ");

        String user = orderToken[0];
        String item = orderToken[1];
        int want = Integer.parseInt(orderToken[2]);

        //delete in orders
        orders.remove(new Integer(id));

        //delete in user_orders
        if(user_orders.containsKey(user)) {
            ArrayList<Integer> o = user_orders.get(user);
            if(o != null){
                if(o.contains(new Integer(id))){
                    o.remove(new Integer(id));
                }
            }
        }

        //update inventory
        if(inventory.containsKey(item)){
            Integer current = inventory.get(item);
            int newCurrent = current.intValue() + want;
            inventory.put(item, new Integer(newCurrent));
        }

        return "Order " + id +" is canceled";
    }

    public synchronized String getOrdersForUser(String user) {

        if(!user_orders.containsKey(user)){
            return "No order found for " + user;
        }
        ArrayList<Integer> o = user_orders.get(user);
        if(o.isEmpty()){
            return "No order found for " + user;
        }

        String response = "";
        for (int i = 0; i < o.size(); i ++){
            Integer oId = o.get(i);
            String order = orders.get(oId);
            String[] orderToken = order.split(" ");
            String build = oId + ", " + orderToken[1] + ", " + orderToken[2];
            response = response + build + "\n";
        }
        return response.substring(0,response.lastIndexOf('\n'));
    }

    public synchronized String readInventory() {
        String response = "";
        for (String key: inventory.keySet()){
            Integer quanlity = inventory.get(key);
            String build = key + " " + quanlity;
            response = response + build + "\n";
        }
        return response.substring(0,response.lastIndexOf('\n'));
    }

    public synchronized void copyInventory(Inventory other) {
        this.user_orders = other.user_orders;
        this.orders = other.orders;
        this.inventory = other.inventory;
        this.orderid = other.orderid;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("INVENTORY ");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            builder.append(entry.getKey());
            builder.append(":");
            builder.append(entry.getValue());
            builder.append(" ");
        }

        builder.append("ORDERS ");
        for (Map.Entry<Integer, String> entry : orders.entrySet()) {
            builder.append(entry.getKey());
            builder.append(":");
            builder.append(entry.getValue());
            builder.append(" ");
        }

        builder.append("USER-ORDERS ");
        for (Map.Entry<String, ArrayList<Integer>> entry : user_orders.entrySet()) {
            builder.append(entry.getKey());
            builder.append(":");

            List<Integer> values =  entry.getValue();
            for (Integer order : values) {
                builder.append(order);
                builder.append(",");
            }

            builder.append(" ");
        }

        builder.append("NUMBER ");
        builder.append(Integer.toString(orderid));

        return builder.toString();
    }

    public static Inventory fromString(String s) {
        String[] tokens = s.split(" ");

        Map<String, Integer> inventory = new HashMap<>();
        Map<Integer, String> orders = new HashMap<>();
        Map<String, ArrayList<Integer>> userOrders = new HashMap<>();
        int orderNumber = 1;

        int index = 0;
        while (index < tokens.length) {
            String word = tokens[index];

            if (word.equals("INVENTORY")) {
                int innerIndex = index + 1;
                while (innerIndex < tokens.length) {
                    word = tokens[innerIndex];
                    if (word.equals("ORDERS")) break;

                    String[] innerTokens = word.split(":");

                    inventory.put(innerTokens[0], Integer.parseInt(innerTokens[1]));
                    innerIndex++;
                }

                index = innerIndex;
            }

            if (word.equals("ORDERS")) {
                int innerIndex = index + 1;
                while (innerIndex < tokens.length) {
                    word = tokens[innerIndex];
                    if (word.equals("USER-ORDERS")) break;

                    String[] innerTokens = word.split(":");

                    orders.put(Integer.parseInt(innerTokens[0]), innerTokens[1]);
                    innerIndex++;
                }

                index = innerIndex;
            }

            if (word.equals("USER-ORDERS")) {
                int innerIndex = index + 1;
                while (innerIndex < tokens.length) {
                    word = tokens[innerIndex];
                    if (word.equals("NUMBER")) break;

                    String[] innerTokens = word.split(":");

                    ArrayList<Integer> orderList = new ArrayList<>();

                    if (innerTokens.length > 1) {
                        String[] orderTokens = innerTokens[1].split(",");
                        for (String orderString : orderTokens) {
                            orderList.add(Integer.parseInt(orderString));
                        }
                    }

                    userOrders.put(innerTokens[0], orderList);
                    innerIndex++;
                }

                index = innerIndex;
            }

            if (word.equals("NUMBER")) {
                index++;
                orderNumber = Integer.parseInt(tokens[index]);
                index++;
            }
        }

        return new Inventory(inventory, orders, userOrders, orderNumber);
    }

}