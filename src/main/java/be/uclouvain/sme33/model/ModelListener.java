package be.uclouvain.sme33.model;

/** Observer receives a fresh immutable list; the View schedules actual drawing on the Swing EDT. */
@FunctionalInterface
public interface ModelListener {
    void onStateChanged(java.util.List<UiElement> currentElements);
}
