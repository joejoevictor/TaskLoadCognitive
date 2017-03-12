import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Application {
    private static final String MAIN_FRAME_TITLE = "Task Load Cognitive Experiment";
    private static final int MAIN_FRAME_WIDTH_IN_PIXELS = 800;
    private static final int MAIN_FRAME_HEIGHT_IN_PIXELS = 600;
    private static final int MAIN_FRAME_MENU_BAR_WIDTH = 800;
    private static final int MAIN_FRAME_MENU_BAR_HEIGHT = 20;
    private static final Logger LOGGER = Logger.getLogger("MainApplication");

    private static final int DEFAULT_NUM_THREADS = 10;

    private TaskLoadCognitiveExperiment experiment;
    private JFrame mainFrame;
    private Application self;
    private Thread applicationThread;
    private Map<Integer, ImagePanel> images;
    private ImagePanel blackBackground;
    private boolean inInstructions;
    private boolean inPreExperimentInstructions;
    private final ExecutorService pool;
    private int currentInstruction = 0;
    private String uid;
    private ExperimentLevel level;
    private int isPracticeMode;

    private final Map<ExperimentLevel, Instruction> instructionMap;

    private Application() {
        inInstructions = false;
        inPreExperimentInstructions = false;
        isPracticeMode = 0;
        experiment = null;
        self = this;
        images = new HashMap<>();
        blackBackground = new ImagePanel("images/black.png");

        instructionMap = new HashMap<>();
        Arrays.asList(ExperimentLevel.values()).forEach(level -> instructionMap.put(level, new Instruction(level)));

        pool = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS);
    }

    private void initNumberImage() {
        for (int i = 1; i <= 8; i++) {
            images.putIfAbsent(i, new ImagePanel("images/number" + i + ".png"));
        }
    }

    private void initBackground() {
        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html>");
        htmlBuilder.append("<h1>Welcome to Task Load Cognitive!</h1>");
        htmlBuilder.append("<h1>PLEASE READ INSTRUCTIONS BEFORE STARTING!</h1>");
        htmlBuilder.append("</html>");
        final JLabel label = new JLabel(htmlBuilder.toString(), SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setForeground(Color.WHITE);
        mainFrame.getContentPane().add(label);
    }

    public void displayTask(final int number) {
        LOGGER.info(String.format("Current displayed number=%s", number));

        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(images.get(number));
        mainFrame.getContentPane().revalidate();
        mainFrame.getContentPane().repaint();
    }

    public void clearScreen() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(blackBackground);
        mainFrame.getContentPane().revalidate();
        mainFrame.getContentPane().repaint();
    }

    private void showInstruction() {
        inPreExperimentInstructions = true;
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(instructionMap.get(level));
        mainFrame.getContentPane().revalidate();
        mainFrame.getContentPane().repaint();
        LOGGER.info(String.format("InstructionShown=%s", level.getName()));

        mainFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                LOGGER.info(String.format("inPreExperimentInstructions=%s", inPreExperimentInstructions));
                if (inPreExperimentInstructions) {
                    inPreExperimentInstructions = false;
                    startExperiment(uid, level);
                }
            }
        });
    }

    private void startExperiment(final String uid, final ExperimentLevel level) {
        experiment = new TaskLoadCognitiveExperiment(uid, level, isPracticeMode, self);
        final Runnable runnable = () -> experiment.start();
        applicationThread = new Thread(runnable);
        pool.submit(applicationThread);
    }

    private void initMenu() {
        // Initialize main menu bar
        final JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setPreferredSize(new Dimension(MAIN_FRAME_MENU_BAR_WIDTH, MAIN_FRAME_MENU_BAR_HEIGHT));

        final JMenu menu = new JMenu("Main Menu");
        menuBar.add(menu);

        // Initialize menu item
        final JMenuItem instructionMenu = new JMenuItem("Read Instructions");
        final JMenuItem start = new JMenuItem("Start Experiment");
        final JMenuItem exit = new JMenuItem("Exit");
        menu.add(instructionMenu);
        menu.add(start);
        menu.add(exit);

        instructionMenu.addActionListener(e -> {
            if (inInstructions) {
                return;
            }

            final ExperimentLevel[] levels = ExperimentLevel.values();
            inInstructions = true;
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(instructionMap.get(levels[currentInstruction++]));
            mainFrame.getContentPane().revalidate();
            mainFrame.getContentPane().repaint();

            mainFrame.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {

                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (!inInstructions || (experiment != null && !experiment.getStarted()) || inPreExperimentInstructions) {
                        return;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        if (currentInstruction >= levels.length) {
                            currentInstruction = 0;
                            resetToStartingPage();
                        } else {
                            mainFrame.getContentPane().removeAll();
                            mainFrame.getContentPane().add(instructionMap.get(levels[currentInstruction++]));
                            mainFrame.getContentPane().revalidate();
                            mainFrame.getContentPane().repaint();
                            LOGGER.info("Instruction: " + currentInstruction);
                        }
                    }
                }
            });
        });

        start.addActionListener(e -> {
            System.out.println("Starting experiment");

            uid = JOptionPane.showInputDialog(
                    mainFrame,
                    "Enter your User ID",
                    "Enter UID",
                    JOptionPane.NO_OPTION
            );
            if (uid == null) {
                resetToStartingPage();
                return;
            }
            LOGGER.info(String.format("UID=%s", uid));

            final ExperimentLevel[] levels = new ExperimentLevel[] {
                    ExperimentLevel.EASY,
                    ExperimentLevel.MEDIUM,
                    ExperimentLevel.DIFFICULT
            };
            level = (ExperimentLevel) JOptionPane.showInputDialog(
                    mainFrame,
                    "Choose task level",
                    "Choose Level",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    levels,
                    levels[0]
            );
            if (level == null) {
                resetToStartingPage();
                return;
            }
            isPracticeMode = JOptionPane.showConfirmDialog(
                mainFrame,
                "Do you want to practice? If so, click 'Yes' and the result of this run won't be recorded.",
                "Practice mode",
                    JOptionPane.YES_NO_OPTION
                    );
            LOGGER.info(String.format("isPracticeMode=%s", isPracticeMode));
            LOGGER.info(String.format("Level=%s", level.getName()));
            showInstruction();
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (experiment != null) {
                    experiment.stop();
                }
                mainFrame.dispose();
                pool.shutdown();
            }
        });

        mainFrame.setJMenuBar(menuBar);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void waitForSpace() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            // Anonymous class invoked from EDT
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                    latch.countDown();
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        latch.await();  // current thread waits here until countDown() is called
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }

    public void endExperiment() {
        JOptionPane.showMessageDialog(mainFrame.getContentPane(), "The experiment has end");
        resetToStartingPage();
    }

    private void resetToStartingPage() {
        inInstructions = false;
        mainFrame.getContentPane().removeAll();
        initBackground();
        mainFrame.getContentPane().revalidate();
        mainFrame.getContentPane().repaint();
    }

    private void initUI() {
        if (mainFrame != null) {
            LOGGER.info("Main frame is already initialized!");
            return;
        }

        mainFrame = new JFrame(MAIN_FRAME_TITLE);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(new Dimension(MAIN_FRAME_WIDTH_IN_PIXELS, MAIN_FRAME_HEIGHT_IN_PIXELS));
        mainFrame.setVisible(true);
        mainFrame.setFocusable(true);
        mainFrame.setFocusTraversalKeysEnabled(false);

        mainFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!inInstructions && experiment != null) {
                    experiment.validateKeyPress(e);
                }
            }
        });
    }

    public static void main(String[] args) {
        System.out.println("Starting TaskLoadCognitive UI");

        final Application application = new Application();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                application.initUI();
                application.initNumberImage();
                application.initBackground();
                application.initMenu();
            }
        });

    }
}
