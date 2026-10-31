import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Entry point. GUI by default, or --cli for headless testing and deterministic demonstrations. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Controller controller = new Controller();
        List<String> flags = Arrays.asList(args);
        boolean useUi = !flags.contains("--cli") && !GraphicsEnvironment.isHeadless();
        if (useUi && !controller.enableUIView()) System.err.println("Swing window unavailable.");
        if (flags.contains("--cli") || flags.contains("--console") || !useUi) runConsole(controller);
    }

    private static void runConsole(Controller controller) {
        System.out.println("Smart Learning (SME-33). Type 'help'.");
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!input.hasNextLine()) return;
            String line = input.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                controller.disableUIView();
                return;
            }
            try { execute(controller, line); }
            catch (IllegalArgumentException exception) { System.out.println("Invalid input: " + exception.getMessage()); }
        }
    }

    static void execute(Controller controller, String line) {
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String argument = parts.length > 1 ? parts[1].trim() : "";
        switch (command) {
            case "help" -> System.out.println("Commands: help | status | switch english/physics/general | "
                    + "start easy/medium/hard | answer 1..4 | open home/shop/profile/stats/flashcards/settings | "
                    + "buy stats/premium/refill | activate NAME[,NAME]* | deactivate NAME[,NAME]* | "
                    + "change +NAME,-NAME (atomic) | ui on/off | quit");
            case "status" -> {
                for (String entry : controller.getStateAsLog()) System.out.println(entry);
            }
            case "switch" -> {
                Subject subject = switch (argument.toLowerCase(Locale.ROOT)) {
                    case "english" -> Subject.ENGLISH;
                    case "physics" -> Subject.PHYSICS;
                    case "general", "knowledge", "general knowledge" -> Subject.GENERAL_KNOWLEDGE;
                    default -> throw new IllegalArgumentException("Unknown subject");
                };
                System.out.println("Subject changed: " + controller.chooseSubject(subject));
            }
            case "start" -> System.out.println("Lesson started: " + controller.startLesson(
                    Difficulty.valueOf(argument.toUpperCase(Locale.ROOT))));
            case "answer" -> {
                int index = Integer.parseInt(argument);
                if (index < 1 || index > 4) throw new IllegalArgumentException("Use answer 1..4");
                System.out.println("Correct answer: " + controller.submitAnswer(index - 1));
                System.out.println(controller.notice());
            }
            case "open" -> System.out.println("Opened: " + controller.open(
                    LearningModel.Screen.valueOf(argument.toUpperCase(Locale.ROOT))));
            case "buy" -> {
                boolean ok = switch (argument.toLowerCase(Locale.ROOT)) {
                    case "stats" -> controller.buyStats();
                    case "premium" -> controller.buyPremium();
                    case "refill" -> controller.refillLives();
                    default -> throw new IllegalArgumentException("Unknown shop item");
                };
                System.out.println("Purchase successful: " + ok);
                System.out.println(controller.notice());
            }
            case "activate" -> System.out.println("Feature result code: " +
                    controller.activate(new String[0], csv(argument)));
            case "deactivate" -> System.out.println("Feature result code: " +
                    controller.activate(csv(argument), new String[0]));
            case "change" -> {
                List<String> enable = new ArrayList<>();
                List<String> disable = new ArrayList<>();
                for (String token : csv(argument)) {
                    if (token.startsWith("+")) enable.add(token.substring(1));
                    else if (token.startsWith("-")) disable.add(token.substring(1));
                    else throw new IllegalArgumentException("Use +FEATURE,-FEATURE");
                }
                System.out.println("Feature result code: " +
                        controller.activate(disable.toArray(String[]::new), enable.toArray(String[]::new)));
            }
            case "ui" -> System.out.println("UI success: " + (argument.equals("on")
                    ? controller.enableUIView() : argument.equals("off")
                    ? controller.disableUIView() : false));
            default -> System.out.println("Unknown command. Type help.");
        }
    }

    private static String[] csv(String argument) {
        if (argument.isBlank()) throw new IllegalArgumentException("Missing feature name");
        return Arrays.stream(argument.split(",", -1)).map(String::trim).toArray(String[]::new);
    }
}
