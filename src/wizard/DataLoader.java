package wizard;

import java.io.IOException;
import java.io.ObjectInputStream;
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
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataLoader {
    public static EnumMap<PlayerStats, Float> loadPlayerObject(String file) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(file);
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("This method is not supposed to read a directory");

        if (Files.exists(path) && Files.isReadable(path))
            try (ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(path))) {
                Object object = objectInputStream.readObject();
                if (object instanceof EnumMap)
                    return (EnumMap<PlayerStats, Float>) object;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        return null;
    }

    public static EnumMap<PlayerStats, Float> loadPlayerIni(String file) {
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(file);
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("This method is not supposed to read a directory");

        EnumMap<PlayerStats, Float> map = new EnumMap<PlayerStats, Float>(PlayerStats.class);
        try (Scanner scanner = new Scanner(Files.newInputStream(path))) {
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                int seperator = nextLine.indexOf('=');
                String enumString = nextLine.substring(0, seperator);
                String valueString = nextLine.substring(seperator + 1);
                PlayerStats playerStat = PlayerStats.valueOf(enumString);
                float statValue = Float.parseFloat(valueString);
                map.put(playerStat, statValue);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return map;
    }
}
