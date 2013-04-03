package wizard;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
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
                if (seperator == -1)
                    continue;

                String enumString = nextLine.substring(0, seperator).trim();
                String valueString = nextLine.substring(seperator + 1);
                PlayerStats playerStat = PlayerStats.valueOf(enumString);
                float statValue = Float.parseFloat(valueString);
                map.put(playerStat, statValue);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        EnumSet<PlayerStats> playerStatses = EnumSet.complementOf(EnumSet.copyOf(map.keySet()));
        for (PlayerStats playerStatse : playerStatses) {
            map.put(playerStatse, Float.NaN);
        }
        return map;
    }

    public static EnumMap<PlayerStats, Float> loadPlayerLua(String file) {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByExtension(".lua");

        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(file);
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("This method is not supposed to read a directory");

        EnumMap<PlayerStats, Float> map = new EnumMap<PlayerStats, Float>(PlayerStats.class);
        try {
            engine.eval(new InputStreamReader(Files.newInputStream(path)));
            for (PlayerStats a : PlayerStats.values()) {
                Object value = engine.get(a.toString());
                if (value == null) {
                    System.out.println("WARNING, Player Stat " + a + " is not set!");
                    continue;
                }
                //System.out.println(value + " " + value.getClass());
                if (value instanceof Double)
                    map.put(a, ((Double) value).floatValue());
                else if (value instanceof Integer)
                    map.put(a, ((Integer) value).floatValue());
                else System.out.println("WARNING, Player Stat " + a + " is not set because it is not a number");
                //float v = Float.parseFloat(value.toString());
            }
        } catch (ScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return map;
    }

}
