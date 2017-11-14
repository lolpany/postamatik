package lol.lolpany.postamatik;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class JsonConfigWatcher<T> implements Runnable {
    private final Class<T> configClass;
    private final Gson gson;
    private final ComponentConnection<T> jsonConfigQueue;
    private AtomicBoolean on;
    private final String configDir;
    private final String fileName;

    public JsonConfigWatcher(Class<T> configClass, String configDir,
                             ComponentConnection<T> jsonConfigQueue, String fileName, Gson gson, AtomicBoolean on) {
        this.configClass = configClass;
        this.configDir = configDir;
        this.jsonConfigQueue = jsonConfigQueue;
        this.fileName = fileName;
        this.gson = gson;
        this.on = on;
    }

    @Override
    public void run() {
        try {
            jsonConfigQueue.offer(gson.fromJson(new FileReader(configDir + File.separator
                    + fileName), configClass));
            WatchKey key = Paths.get(configDir).register(
                    FileSystems.getDefault().newWatchService(), ENTRY_MODIFY);
            while (on.get()) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) {
                        continue;
                    }

                    if (event.kind() == ENTRY_MODIFY) {
                        if (((Path) event.context()).endsWith(fileName)) {
                            try {
                                jsonConfigQueue.offer(gson.fromJson(new FileReader(configDir + File.separator
                                        + fileName), configClass));
                            } catch (JsonSyntaxException | IOException | JsonIOException e) {
                                // ignore
                            }
                        }

                    }
                }
                sleep(1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
