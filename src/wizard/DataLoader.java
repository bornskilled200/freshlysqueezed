package wizard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 3/11/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataLoader {
    public static EnumMap<PlayerStats,Float> loadPlayer(String file)
    {
        FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(file);
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("This method is not supposed to read a directory");

        if (Files.exists(path) && Files.isReadable(path))
            try(ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(path))) {
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
}
