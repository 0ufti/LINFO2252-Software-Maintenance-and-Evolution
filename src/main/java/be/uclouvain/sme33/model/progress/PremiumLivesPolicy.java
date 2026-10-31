package be.uclouvain.sme33.model.progress;

public final class PremiumLivesPolicy implements LivesPolicy {
    @Override public boolean canStart(Progress progress) { return true; }
    @Override public void wrongAnswer(Progress progress) { /* premium lives are infinite */ }
    @Override public String label(Progress progress) { return "Lives: unlimited"; }
}
