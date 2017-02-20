import javax.swing.*;
import java.awt.*;

public class Main {
    private static final String MAIN_FRAME_TITLE = "Task Load Cognitive";
    private static final int MAIN_FRAME_WIDTH_IN_PIXELS = 800;
    private static final int MAIN_FRAME_HEIGHT_IN_PIXELS = 600;

    private static JFrame createAndShowMainFrame(){
        final JFrame jFrame = new JFrame(MAIN_FRAME_TITLE);
        addMenuBar(jFrame);
        addContentPane(jFrame);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setSize(MAIN_FRAME_WIDTH_IN_PIXELS, MAIN_FRAME_HEIGHT_IN_PIXELS);
        jFrame.setVisible(Boolean.TRUE);
        return jFrame;
    }

    private static void addMenuBar(final JFrame jFrame) {
        final JMenuBar jMenuBar = new JMenuBar();
        final JMenu jMenu = new JMenu("A Menu");
        final JMenuItem restartMenuItem = new JMenuItem("Restart");
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        jMenu.add(restartMenuItem);
        jMenu.add(exitMenuItem);
        jFrame.setJMenuBar(jMenuBar);
    }

    private static void addContentPane(final JFrame jFrame) {
        final JPanel jPanel = new JPanel(new BorderLayout());

        final JTextArea textArea = new JTextArea(5, 30);
        textArea.setEditable(true);
        final JScrollPane scrollPane = new JScrollPane(textArea);

        jPanel.add(scrollPane, BorderLayout.CENTER);
        jPanel.setOpaque(Boolean.TRUE);
        jFrame.setContentPane(jPanel);
    }

    public static void main(String[] args) {
        System.out.println("Starting TaskLoadCognitive UI");

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = createAndShowMainFrame();

                System.out.println("GUI Started");
            }
        });

    }
}
