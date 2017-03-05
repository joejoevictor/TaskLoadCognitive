import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class TaskLoadCognitiveExperiment {
    private static final Logger LOGGER = Logger.getLogger(TaskLoadCognitiveExperiment.class.getName());
    private static final int DEFAULT_NUMBER_OF_NUMBERS = 50;
    private static final long FOUR_MINUTES_IN_MILLIS = 4 * 60 * 1000;
    private static final long PAUSE_INTERVAL_SECONDS_IN_MILLIS = 1600;
    private static final long DISPLAY_INTERVAL_SECONDS_IN_MILLIS = 500;

    private final String uid;
    private int[] numbers;
    private Task[] tasks;
    private int score;
    private int currentStep;
    private Application application;
    private volatile boolean shouldStop = false;
    private ExperimentLevel level;
    private Task currentTask;
    private boolean started;

    private static final List<String> CSV_HEADERS = new ArrayList<String>() {{
        add("Task");
        add("Number");
        add("Correctness");
        add("Reaction Time");
    }};

    public TaskLoadCognitiveExperiment(final String uid,
                                       final ExperimentLevel level,
                                       final Application application) {
        this(uid, DEFAULT_NUMBER_OF_NUMBERS, level, application);
    }

    public TaskLoadCognitiveExperiment(final String uid,
                                       final int numOfNumbers,
                                       final ExperimentLevel level,
                                       final Application application) {
        this.uid = uid;
        this.score = 0;
        this.level = level;
        this.currentStep = 0;
        this.numbers = new int[numOfNumbers];
        this.tasks = new Task[numOfNumbers];
        this.application = application;
        this.shouldStop = false;
        this.currentTask = null;
        this.started = false;
        generateNumbers();
        initTasks();
    }

    private boolean isOnWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    private void writeInputCSV() {
        final Date now = new Date();
        final TimeZone tz = TimeZone.getTimeZone("PST");
        final DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);

        final StringBuilder fileNameStringBuilder = new StringBuilder();
        fileNameStringBuilder.append(isOnWindows() ? "C:\\tmp\\" : "/tmp/");
        fileNameStringBuilder.append(this.uid);
        fileNameStringBuilder.append("_");
        fileNameStringBuilder.append(df.format(now));
        fileNameStringBuilder.append(".csv");
        final String outputFileName = fileNameStringBuilder.toString();
        LOGGER.info("Output File: " + outputFileName);
        try {
            final File file = new File(outputFileName);
            final FileWriter fileWriter = new FileWriter(file);
            final StringBuilder csvBuilder = new StringBuilder();
            csvBuilder.append("UID: ");
            csvBuilder.append(this.uid);
            csvBuilder.append(", Experiment Time: ");
            csvBuilder.append(now.toString());
            csvBuilder.append("\n");

            for (int i = 0; i < CSV_HEADERS.size(); i++) {
                csvBuilder.append(CSV_HEADERS.get(i));
                if (i < CSV_HEADERS.size() - 1) {
                    csvBuilder.append(",");
                }
            }
            csvBuilder.append("\n");

            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i].getCorrect() != null ) {
                    csvBuilder.append(Integer.toString(i));
                    csvBuilder.append(",");
                    csvBuilder.append(Integer.toString(numbers[i]));
                    csvBuilder.append(",");
                    csvBuilder.append(Boolean.toString(tasks[i].getCorrect()));
                    csvBuilder.append(",");
                    csvBuilder.append(tasks[i].getReactionTime() == null ? "N/A" : Long.toString(tasks[i].getReactionTime()));
                    csvBuilder.append("\n");
                }
            }

            fileWriter.write(csvBuilder.toString());
            fileWriter.close();
        } catch (final IOException e) {
            LOGGER.severe("Failed to write to " + outputFileName);
        }
    }

    public void validateKeyPress(final KeyEvent e) {
        if (!this.started) {
            return;
        }

        if (currentTask == null) {
            throw new RuntimeException("Current task is null, this is terrible!");
        }

        if (!currentTask.isStarted() || currentTask.isEnded()) {
            LOGGER.warning("Task is not started or task is ended");
            return;
        }

        switch (level) {
            case EASY:
                if (numbers[currentStep] != 5) {
                    currentTask.setCorrect(false);
                    return;
                }

                if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                    // Check current task in experiment
                    currentTask.setCorrect(false);
                } else {
                    currentTask.setCorrect(true);
                    currentTask.setReactionTime(System.currentTimeMillis());
                }
                break;

            case MEDIUM:
                if (currentStep - 2 < 0
                        || !(isEvenNumber(numbers[currentStep])
                                && isEvenNumber(numbers[currentStep - 1])
                                && isEvenNumber(numbers[currentStep - 2])
                            )) {
                    currentTask.setCorrect(false);
                    return;
                }

                if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                    // Check current task in experiment
                    currentTask.setCorrect(false);
                } else {
                    currentTask.setCorrect(true);
                    currentTask.setReactionTime(System.currentTimeMillis());
                }
                break;

            case DIFFICULT:
                if (currentStep - 2 < 0 || numbers[currentStep] != numbers[currentStep - 2]) {
                    currentTask.setCorrect(false);
                    return;
                }

                if (e.getKeyCode() != KeyEvent.VK_SPACE) {
                    // Check current task in experiment
                    currentTask.setCorrect(false);
                } else {
                    currentTask.setCorrect(true);
                    currentTask.setReactionTime(System.currentTimeMillis());
                }

                break;

            default:
                throw new RuntimeException("This should not happen");
        }
    }

    public void stop() {
        shouldStop = true;
    }

    public void start() {
        LOGGER.info(String.format("Starting experiment with user uid=%s", uid));

        final long experimentStartTime = System.currentTimeMillis();
        final long experimentEndTime = experimentStartTime + FOUR_MINUTES_IN_MILLIS;

        while (!shouldStop) {

            if (System.currentTimeMillis() > experimentEndTime) {
                break;
            }

            application.clearScreen();
            currentTask = tasks[currentStep];
            started = true;

            if (currentTask == null) {
                LOGGER.severe("Current task is null, this is terrible!");
                break;
            }

            try {
                Thread.sleep(PAUSE_INTERVAL_SECONDS_IN_MILLIS);
            } catch (final InterruptedException e) {
                LOGGER.warning("Thread interrupted");
            }
            currentTask.setStarted();
            currentTask.setStartTime(System.currentTimeMillis());

            application.displayTask(numbers[currentStep]);
            final long endTime = System.currentTimeMillis() + DISPLAY_INTERVAL_SECONDS_IN_MILLIS;

            while (!shouldStop) {
                if (System.currentTimeMillis() > endTime) {
                    break;
                }
            }
            currentTask.setEnded();

            switch (level) {
                case EASY:
                    if (numbers[currentStep] != 5) {
                        currentTask.setCorrect(true);
                        break;
                    }

                    currentTask.setCorrect(false);
                    break;

                case MEDIUM:
                    if (currentStep < 2) {
                        currentTask.setCorrect(true);
                        break;
                    }

                    if (!(isEvenNumber(numbers[currentStep])
                            && isEvenNumber(numbers[currentStep - 1])
                            && isEvenNumber(numbers[currentStep - 2]))) {
                        currentTask.setCorrect(true);
                    }

                    currentTask.setCorrect(false);
                    break;

                case DIFFICULT:
                    if (currentStep < 2) {
                        currentTask.setCorrect(true);
                        break;
                    }

                    if (numbers[currentStep] != numbers[currentStep - 2]) {
                        currentTask.setCorrect(true);
                    }

                    currentTask.setCorrect(false);
                    break;
                default:
                    throw new RuntimeException("This should not happen!");
            }

            currentStep++;
        }

        LOGGER.info("Experiment finished");
//        printResult();
        writeInputCSV();
    }

    private void printResult() {
        for (int i = 0; i < tasks.length; i++) {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("[Task ");
            stringBuilder.append(i);
            stringBuilder.append("]: ");
            stringBuilder.append("Correct: ");
            stringBuilder.append(tasks[i].getCorrect() == null ? "N/A Not started" : tasks[i].getCorrect() + " ");
            stringBuilder.append(tasks[i].getReactionTime() == null ? "" : ("Reaction Time: " + tasks[i].getReactionTime() + "ms"));

            LOGGER.info("[Task " + i + "]: Correct: " + tasks[i].getCorrect() + "\n");
            if (tasks[i].getReactionTime() != null) {
                LOGGER.info("Reaction Time: " + tasks[i].getReactionTime());
            }
        }
    }

    private void generateNumbers() {
        final Random random = new Random();
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = random.nextInt(8) + 1;
        }
    }

    private void initTasks() {
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new Task();
        }
    }

    public int getScore() {
        return this.score;
    }

    public void incrementScore() {
        this.score++;
    }

    public int getCurrentStep() {
        return this.currentStep;
    }

    public void toNextStep() {
        this.currentStep++;
    }

    private boolean isEvenNumber(final int num) {
        return num % 2 == 0;
    }
}
