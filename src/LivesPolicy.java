// Strategy : lives can be handled multiple ways
interface LivesPolicy {
    boolean canPlay(Progress progress);
    void wrongAnswer(Progress progress);
    String displayLives(Progress progress);
}

class FreeLives implements LivesPolicy {
    public boolean canPlay(Progress progress) { return progress.getLives() > 0; }
    public void wrongAnswer(Progress progress) { progress.loseLife(); }
    public String displayLives(Progress progress) { return "Lives: " + progress.getLives() + "/5"; }
}

class PremiumLives implements LivesPolicy {
    public boolean canPlay(Progress progress) { return true; }
    public void wrongAnswer(Progress progress) { /* infinite lives */ }
    public String displayLives(Progress progress) { return "Lives: unlimited"; }
}
