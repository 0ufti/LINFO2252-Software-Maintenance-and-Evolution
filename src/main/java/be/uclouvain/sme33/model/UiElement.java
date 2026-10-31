package be.uclouvain.sme33.model;

/** One immutable UI specification, shared by state logs and the Swing renderer. */
public record UiElement(Kind kind, String id, String text, Area area) {
    public enum Kind { HEADING, TEXT, BUTTON, BADGE }
    public enum Area { HEADER, NAVIGATION, MAIN, FOOTER }

    public UiElement {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Missing element ID");
        if (text == null) throw new IllegalArgumentException("Missing text");
    }

    /** State description, not an event history; field separator is escaped. */
    public String toLog() {
        return kind.name().toLowerCase() + "|" + escape(id) + "|" + escape(text) + "|" + area.name();
    }
    private static String escape(String value) { return value.replace("%", "%25").replace("|", "%7C"); }
}
