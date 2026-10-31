package be.uclouvain.sme33.model.progress;

public final class FreeLivesPolicy implements LivesPolicy {
    @Override public boolean canStart(Progress progress) { return progress.lives() > 0; }
    @Override public void wrongAnswer(Progress progress) { progress.loseLife(); }
    @Override public String label(Progress progress) { return "Lives: " + progress.lives() + "/" + Progress.MAX_LIVES; }
}
