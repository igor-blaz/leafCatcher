package leafCatcher.history;

import lombok.Data;

@Data
public class Draft {
    private String rootDescription;
    private String rootButtonName;

    private String endDescription;
    private String endButtonName;

    private String childDescription;
    private String childButtonName;

    private Integer depthCounter;

    public void increaseDepthCounter() {
        depthCounter++;
    }

    public void decreaseDepthCounter() {
        depthCounter--;
    }

    public void makeZeroDepthCounter() {
        depthCounter = 0;
    }
}
