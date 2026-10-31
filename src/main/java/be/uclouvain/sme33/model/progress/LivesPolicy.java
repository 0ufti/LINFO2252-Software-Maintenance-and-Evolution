package be.uclouvain.sme33.model.progress;

/** Strategy: free and premium change the same interaction without changing the Controller. */
public interface LivesPolicy {
    boolean canStart(Progress progress);
    void wrongAnswer(Progress progress);
    String label(Progress progress);
}
