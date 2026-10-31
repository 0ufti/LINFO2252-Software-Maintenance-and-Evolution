package be.uclouvain.sme33.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Incremental logger: each UI region owns entries. updateGroup edits only changed entries.
 * Feature transitions do not discard and rebuild the entire log; old state is not retained.
 */
public final class UiStateLog {
    private final Map<String, Map<String, UiElement>> groups = new LinkedHashMap<>();

    public void updateGroup(String groupId, Collection<UiElement> current) {
        Map<String, UiElement> target = new LinkedHashMap<>();
        for (UiElement entry : current) {
            if (target.putIfAbsent(entry.id(), entry) != null) {
                throw new IllegalArgumentException("Duplicate UI element ID in " + groupId + ": " + entry.id());
            }
        }
        Map<String, UiElement> existing = groups.computeIfAbsent(groupId, ignored -> new LinkedHashMap<>());
        existing.keySet().removeIf(id -> !target.containsKey(id));
        target.forEach((id, element) -> {
            if (!element.equals(existing.get(id))) existing.put(id, element);
        });
    }

    public List<UiElement> elements() {
        List<UiElement> result = new ArrayList<>();
        groups.values().forEach(map -> result.addAll(map.values()));
        return Collections.unmodifiableList(result);
    }
    public String[] stateAsLog() {
        return elements().stream().map(UiElement::toLog).toArray(String[]::new);
    }
}
