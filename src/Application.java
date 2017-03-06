import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private ImagePanel instructionEasy;
    private ImagePanel instructionMedium;
    private ImagePanel instructionDifficult;
    private boolean inInstructions;
    private final ExecutorService pool;
    private int currentInstruction = 0;

    private java.util.List<ImagePanel> instructions;

    private Application() {
        inInstructions = false;
        experiment = null;
        self = this;
        images = new HashMap<>();
        blackBackground = new ImagePanel("images/black.png");
        instructionEasy = new ImagePanel("images/instructionsEasy.png");
        instructionMedium = new ImagePanel("images/instructionsMedium.png");
        instructionDifficult = new ImagePanel("images/instructionsDifficult.png");
        instructions = new ArrayList<>();
        instructions.add(instructionEasy);
        instructions.add(instructionMedium);
        instructions.add(instructionDifficult);
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

            inInstructions = true;
            mainFrame.getContentPane().removeAll();
            mainFrame.getContentPane().add(instructions.get(currentInstruction++));
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
                    if (!inInstructions || (experiment != null && !experiment.getStarted())) {
                        return;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        if (currentInstruction >= instructions.size()) {
                            currentInstruction = 0;
                            resetToStartingPage();
                        } else {
                            mainFrame.getContentPane().removeAll();
                            mainFrame.getContentPane().add(instructions.get(currentInstruction++));
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

            final String uid = JOptionPane.showInputDialog(
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
            final ExperimentLevel level = (ExperimentLevel) JOptionPane.showInputDialog(
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
            LOGGER.info(String.format("Level=%s", level.getName()));

            if (experiment == null) {
                experiment = new TaskLoadCognitiveExperiment(uid, level, self);
            }

            final Runnable runnable = () -> experiment.start();


            applicationThread = new Thread(runnable);
            pool.submit(applicationThread);

            pool.shutdown();
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
