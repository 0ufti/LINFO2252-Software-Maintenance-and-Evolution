package be.uclouvain.sme33.model.progress;

/** Immutable read-only snapshot: consumers never receive a mutable Progress reference. */
public record ProgressSummary(int lives, int coins, int completedLevels,
                              int correctAnswers, int wrongAnswers, int streak,
                              boolean statisticsPurchased) { }
