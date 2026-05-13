import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        while (true) {
            WelcomeDialog welcome = new WelcomeDialog();
            welcome.setVisible(true);
            if (!welcome.shouldStart()) {
                System.exit(0);
                return;
            }
            int numPlayers = chooseNumPlayers();
            if (numPlayers < 0) {
                continue;
            }
            GameFrame frame = new GameFrame(numPlayers);
            frame.setVisible(true);
            return;
        }
    }

    public static int chooseNumPlayers() {
        StartDialog d = new StartDialog();
        d.setVisible(true);
        return d.getChosenPlayers();
    }
}