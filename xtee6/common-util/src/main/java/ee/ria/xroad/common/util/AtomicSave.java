package ee.ria.xroad.common.util;

import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import ee.ria.xroad.common.DefaultFilepaths;

import static java.nio.file.StandardOpenOption.*;

/**
 * Holds atomic save utility methods.
 */
public final class AtomicSave {

    private AtomicSave() {
    }

    /**
     * Functional interface for a callback that should be executed when data is being saved.
     */
    @FunctionalInterface
    public interface Callback {
        /**
         * Called when data is being atomically saved.
         * @param out output stream where data is written during the atomic save
         * @throws Exception if any errors occur
         */
        void save(OutputStream out) throws Exception;
    }

    /**
     * Atomically executes the given callback as part of the atomic save to the
     * provided filename. If an error occurs no changes to the file will be made.
     * @param fileName filename where data should be atomically saved
     * @param tmpPrefix prefix of the temporary file used in the process
     * @param callback callback that should be executed when data is atomically saved
     * @param options options specifying how the move should be done
     * @throws Exception if any errors occur
     */
    public static void execute(String fileName, String tmpPrefix,
            Callback callback, CopyOption... options) throws Exception {
        Path tempFile = DefaultFilepaths.createTempFile(tmpPrefix, null);

        SeekableByteChannel channel = Files.newByteChannel(tempFile, CREATE,
                WRITE, TRUNCATE_EXISTING, DSYNC);

        try (OutputStream out = Channels.newOutputStream(channel)) {
            callback.save(out);
        }

        Path target = Paths.get(fileName);

        if (options.length == 0) {
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.move(tempFile, target, options);
        }
    }

    /**
     * Atomically writes the given byte array to the file with the provided filename.
     * provided filename. If an error occurs no changes to the file will be made.
     * @param fileName filename where data should be atomically saved
     * @param tmpPrefix prefix of the temporary file used in the process
     * @param data byte array that should be atomically saved in the file
     * @param options options specifying how the move should be done
     * @throws Exception if any errors occur
     */
    public static void execute(String fileName, String tmpPrefix,
            final byte[] data, CopyOption... options) throws Exception {
        execute(fileName, tmpPrefix, out -> out.write(data), options);
    }
}