package findex.watchers;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {
    private final WatchService watcher;
    private final ConcurrentHashMap<WatchKey, Path> keys;
    private final boolean recursive;
    private final boolean trace;

    public FileWatcher(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new ConcurrentHashMap<>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        this.trace = true;
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void addEvents(Queue<Callable<Path>> eventsQueue) {
        while (keys.size() > 0) {
            try {
                final var key = watcher.take();
                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // TBD
                    if (kind == OVERFLOW) { continue; }

                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    System.out.format("%s: %s\n", event.kind().name(), child);

                    if (recursive && (kind == ENTRY_CREATE)) {

                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            } else {
                                eventsQueue.add(() -> child);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (kind == ENTRY_MODIFY) {
                        eventsQueue.add(() -> child);
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    // all directories are inaccessible
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}