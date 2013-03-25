import wizard.DataLoader;
import wizard.PlayerStats;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 3/11/13
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class datasaver {
    private static final String MENU =
            "PRINT the map we have now\n" +
                    "MENU to show it again\n" +
                    "LOAD a map to this program\n" +
                    "SAVE the map to file\n" +
                    "ITERATE all the properties\n" +
                    "EDIT a single property\n" +
                    "EXIT to exit\n";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EnumMap<PlayerStats, Float> enumMap = new EnumMap<PlayerStats, Float>(PlayerStats.class);
        for (PlayerStats a : PlayerStats.values())
            if (!enumMap.containsKey(a))
                enumMap.put(a, Float.NaN);

        String input;
        System.out.println(MENU);
        do {
            input = scanner.next().toLowerCase();
            switch (input) {
                case "print":
                    printMap(enumMap);
                    break;
                case "menu":
                    System.out.println(MENU);
                    break;
                case "load":
                    System.out.print("Type in the file to load: ");
                    EnumMap<PlayerStats, Float> loadedMap = DataLoader.loadPlayerObject(scanner.next());
                    if (loadedMap == null) {
                        System.out.println("Cannot load this file!");
                        break;
                    }
                    enumMap = loadedMap;
                    break;
                case "save":
                    System.out.print("Type in the file to save to: ");
                    saveData(scanner.next(), enumMap, true);
                    break;
                case "iterate":
                    iterateAll(scanner, enumMap);
                    break;
                case "edit":
                    System.out.print("Type in the property: ");
                    input = scanner.next();
                    PlayerStats playerStats = PlayerStats.valueOf(input);
                    if (playerStats == null)
                        System.out.println(input + " is not a property");
                    else {
                        System.out.print("Type in the value");
                        processInput(enumMap, playerStats, scanner.next());
                    }
                    break;
                case "exit":
                    break;
                default:
                    System.out.println("You typed in an invalid menu option\n" + MENU);
                    break;
            }
        }
        while (!input.equals("exit"));
    }

    private static void printMap(EnumMap<PlayerStats, Float> enumMap) {
        for (PlayerStats a : PlayerStats.values())
            System.out.println(a + ": " + enumMap.get(a));
    }

    private static void iterateAll(Scanner scanner, EnumMap<PlayerStats, Float> enumMap) {
        for (PlayerStats a : PlayerStats.values()) {
            System.out.print(a + ": ");
            String next = scanner.next();
            processInput(enumMap, a, next);
        }
    }

    private static void processInput(EnumMap<PlayerStats, Float> enumMap, PlayerStats a, String next) {
        if (next.isEmpty())
            return;

        if (next.startsWith("n"))                               //nan
            enumMap.put(a, Float.NaN);
        else if (next.startsWith("mi") || next.indexOf(0) == '-')    //min
            enumMap.put(a, Float.MIN_VALUE);
        else if (next.startsWith("ma") || next.indexOf(0) == '+')    //max
            enumMap.put(a, Float.MAX_VALUE);
        else if (next.startsWith("-inf"))                       //negative infinity
            enumMap.put(a, Float.NEGATIVE_INFINITY);
        else if (next.startsWith("inf"))                        //positive infinite
            enumMap.put(a, Float.POSITIVE_INFINITY);
        else                                        //assume its a float, hopefully
            enumMap.put(a, Float.parseFloat(next));
    }

    public static void saveData(String file, EnumMap<PlayerStats, Float> map, boolean overwrite) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(file);
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("This method is not supposed to read a directory");

        if (Files.exists(path) && Files.isReadable(path)) {
            if (overwrite == false)
                return;
        } else
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(path))) {
            objectOutputStream.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
