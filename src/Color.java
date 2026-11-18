
// this class is used as a helper to color text in console
public class Color {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    public static String red(String input) { return ANSI_RED + input + ANSI_RESET; }
    public static String black(String input) { return ANSI_BLACK + input + ANSI_RESET; }
    public static String green(String input) { return ANSI_GREEN + input + ANSI_RESET; }
    public static String yellow(String input) { return ANSI_YELLOW + input + ANSI_RESET; }
    public static String blue(String input) { return ANSI_BLUE + input + ANSI_RESET; }
    public static String purple(String input) { return ANSI_PURPLE + input + ANSI_RESET; }
    public static String cyan(String input) { return ANSI_CYAN + input + ANSI_RESET; }
    public static String white(String input) { return ANSI_WHITE + input + ANSI_RESET; }
}