import java.util.HashMap;
import java.util.Map;

public class Instruction extends ImagePanel {
    private static final String EASY_INSTRUCTION_IMAGE_PATH = "images/instructionsEasy.png";
    private static final String MEDIUM_INSTRUCTION_IMAGE_PATH = "images/instructionsMedium.png";
    private static final String DIFFICULT_INSTRUCTION_IMAGE_PATH = "images/instructionsDifficult.png";

    private static final Map<ExperimentLevel, String> IMAGE_PATHS = new HashMap<ExperimentLevel, String>() {{
        put(ExperimentLevel.EASY, EASY_INSTRUCTION_IMAGE_PATH);
        put(ExperimentLevel.MEDIUM, MEDIUM_INSTRUCTION_IMAGE_PATH);
        put(ExperimentLevel.DIFFICULT, DIFFICULT_INSTRUCTION_IMAGE_PATH);
    }};

    private ExperimentLevel level;

    public Instruction(final ExperimentLevel level) {
        super(IMAGE_PATHS.get(level));
        this.level = level;
    }

    public ExperimentLevel getLevel(){
        return this.level;
    }
}
