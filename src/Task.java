public class Task {
    private Boolean started;
    private Boolean ended;
    private Boolean correct;
    private Long startTime;
    private Long reactionTime;
    private Boolean canChangeCorrectness;

    public Task() {
        started = false;
        ended = false;
        correct = null;
        startTime = null;
        reactionTime = null;
        canChangeCorrectness = true;
    }

    public void setStarted() {
        started = true;
    }

    public void setEnded() {
        ended = true;
    }

    public void setCorrect(final boolean correct) {
        if (!canChangeCorrectness) {
            return;
        }

        this.correct = correct;
        canChangeCorrectness = false;
    }

    public Boolean getCorrect() {
        return this.correct;
    }

    public Boolean isStarted() {
        return this.started;
    }

    public Boolean isEnded() {
        return this.ended;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public void setReactionTime(final long reactionEndTime) {
        this.reactionTime = reactionEndTime - startTime;
    }

    public Long getReactionTime() {
        return this.reactionTime;
    }

}
