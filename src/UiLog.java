import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// current items on screen
public class UiLog {
    public static class Item {
        public String type, id, text, place;
        public Item(String type, String id, String text, String place) {
            this.type = type;
            this.id = id;
            this.text = text;
            this.place = place;
        }
        public String toString() {
            return type + "|" + id + "|" + text.replace("|", "/") + "|" + place;
        }
    }

    private Map<String, Map<String, Item>> parts = new LinkedHashMap<>();

    public void update(String name, Collection<Item> items) {
        Map<String, Item> old = parts.computeIfAbsent(name, k -> new LinkedHashMap<>());
        Map<String, Item> wanted = new LinkedHashMap<>();
        for (Item item : items) wanted.put(item.id, item);
        old.keySet().removeIf(id -> !wanted.containsKey(id));
        for (Item item : wanted.values()) old.put(item.id, item);
    }

    public List<Item> getItems() {
        List<Item> list = new ArrayList<>();
        for (Map<String, Item> group : parts.values()) list.addAll(group.values());
        return list;
    }

    public String[] getLines() {
        List<String> result = new ArrayList<>();
        for (Item item : getItems()) result.add(item.toString());
        return result.toArray(new String[0]);
    }
}
