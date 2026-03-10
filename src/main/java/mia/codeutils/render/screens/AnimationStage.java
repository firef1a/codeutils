package mia.codeutils.render.screens;

public enum AnimationStage {
    OPENING(1),
    OPEN(0),
    CLOSING(-1),
    CLOSED(0);

    public int direction;

    AnimationStage(int direction) {
        this.direction = direction;
    }
}
