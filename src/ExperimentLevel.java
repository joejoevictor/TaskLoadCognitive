import java.util.HashMap;
import java.util.Map;

public enum ExperimentLevel {
    EASY("Easy"),
    MEDIUM("Medium"),
    DIFFICULT("Difficult");

    private final String name;
    public static Map<String, ExperimentLevel> dict;

    ExperimentLevel(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    static {
        dict = new HashMap<>();
        dict.put("East", ExperimentLevel.EASY);
        dict.put("Medium", ExperimentLevel.MEDIUM);
        dict.put("Difficult", ExperimentLevel.DIFFICULT);
    }
}
