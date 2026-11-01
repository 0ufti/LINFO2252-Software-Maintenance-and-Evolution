import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Smart Learning App");
        System.out.println("Type help to see the commands.");
        show(controller);

        while (true) {
            System.out.print("\n> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) break;
            try {
                command(controller, line);
            } catch (Exception e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }
        }

        scanner.close();
        controller.disableUIView();
    }

    public static void command(Controller c, String line) {
        String[] parts = line.split("\\s+", 2);
        String action = parts[0].toLowerCase();
        String value = parts.length == 2 ? parts[1].trim() : "";
        boolean success;

        switch (action) {
            case "help":
                System.out.println("(select a subject) switch [ english || physics || general ]");
                System.out.println("(select a lesson)  start [ easy || medium || hard ]");
                System.out.println("(select an answer) answer [ 1..4 ]");
                System.out.println("(change page)      home | shop | profile | stats | flashcards | settings");
                System.out.println("(buy items)        buy [ stats || premium || refill ]");
                System.out.println("(manage features)  activate _feature_ / deactivate _feature_ / change +_feature_,-_feature_");
                System.out.println("(UI)               ui [ on || off ]");
                System.out.println("(others)           status logs exit");
                break;

            case "switch":
                value = value.toLowerCase();
                if (value.equals("general") || value.equals("general knowledge")) value = "GENERAL KNOWLEDGE";
                success = c.chooseSubject(value);
                if (!success) System.out.println("Could not select that subject.");
                show(c);
                break;

            case "start":
                success = c.startLesson(value);
                if (!success) System.out.println("Could not start: " + c.getMessage());
                show(c);
                break;

            case "answer":
                int number = Integer.parseInt(value);
                if (number < 1 || number > 4) {
                    System.out.println("Choose 1, 2, 3 or 4.");
                } else {
                    c.answer(number - 1);
                    show(c);
                }
                break;

            case "home": case "shop": case "profile": case "stats": case "flashcards": case "settings": case "open":
                if (action.equals("open")) action = value.toLowerCase();
                if (!c.open(action)) System.out.println("Not yet available.");
                show(c);
                break;

            case "buy":
                if (!c.buy(value)) System.out.println("Purchase failed.");
                show(c);
                break;

            case "activate":
                printCode(c.activate(new String[0], featureNames(value)));
                show(c);
                break;

            case "deactivate":
                printCode(c.activate(featureNames(value), new String[0]));
                show(c);
                break;

            case "change":
                List<String> off = new ArrayList<>();
                List<String> on = new ArrayList<>();

                for (String token : value.split(",")) {
                    token = token.trim();
                    if (token.startsWith("+")) on.add(token.substring(1));
                    else if (token.startsWith("-")) off.add(token.substring(1));
                    else { System.out.println("Use + or - before each feature."); return; }
                }

                printCode(c.activate(off.toArray(new String[0]), on.toArray(new String[0])));
                show(c);
                break;

            case "status":
                show(c);
                break;

            case "logs":
                for (String log : c.getStateAsLog()) System.out.println(log);
                break;

            case "ui":
                if (value.equalsIgnoreCase("on")) System.out.println("Window opened: " + c.enableUIView());
                else if (value.equalsIgnoreCase("off")) System.out.println("Window closed: " + c.disableUIView());
                else System.out.println("Use ui on or ui off.");
                break;

            default:
                System.out.println("Unknown command. Type help.");
        }
    }

    private static String[] featureNames(String value) {
        if (value.isEmpty()) return new String[]{""};
        return value.split(",");
    }

    // translates codes to readable meaning
    private static void printCode(int code) {
        if (code == 0) System.out.println("OK");
        else if (code == 1) System.out.println("Unknown feature (not in our feature model).");
        else if (code == 2) System.out.println("This feature is not coded yet.");
        else if (code == 3) System.out.println("Invalid combination.");
        else if (code == 4) System.out.println("You need Premium or to buy the feature.");
    }

    // pretty display of controller state
    // ps not so pretty code.
    private static void show(Controller c) {
        LearningModel m = c.getModel();
        Progress p = m.getProgress();
        System.out.println("-----------------------------");
        System.out.println(m.getSubject() + " | " + m.getLivesText() + " | Coins: " + p.getCoins());
        if (m.isOn("PREMIUM")) System.out.println("[Premium]");
        if (m.isOn("STREAKS")) System.out.println("Streak: " + p.getStreak());
        if (m.isOn("DAILY GOAL")) System.out.println("Daily goal: " + p.getTodayDone(java.time.LocalDate.now()) + "/1");
        String screen = c.getScreen();
        if (screen.equals("HOME")) {
            System.out.println("Choose: easy, medium, hard");
        } else if (screen.equals("QUESTION")) {
            Question q = c.getQuestion();
            System.out.println(m.getDifficulty() + ": " + q.getText());
            String[] a = q.getAnswers();
            for (int i = 0; i < a.length; i++) System.out.println((i + 1) + ". " + a[i]);
        } else if (screen.equals("SHOP")) {
            System.out.println("Shop: stats (12), premium (24), refill (5) coins");
        } else if (screen.equals("PROFILE")) {
            System.out.println("Profile: Demo learner | completed " + p.getFinishedCount() + "/9");
            if (m.isOn("PROFILE SKIN")) System.out.println("Premium skin");
        } else if (screen.equals("STATS")) {
            System.out.println("Correct: " + p.getGoodAnswers() + " | Wrong: " + p.getBadAnswers()
                + " | Completed: " + p.getFinishedCount() + "/9");
        } else if (screen.equals("FLASHCARDS")) {
            System.out.println("Flash card: " + m.getSubject());
            for (String s : c.getStateAsLog()) if (s.contains("flash.")) System.out.println(s.split("\\|", 4)[2]);
        } else if (screen.equals("SETTINGS")) {
            System.out.println("Theme: " + (m.isOn("THEME") ? "dark" : "light"));
        }
        if (!c.getMessage().isEmpty()) System.out.println(c.getMessage());
    }
}
