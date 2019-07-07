package dev.douglassparker.pixels;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import reactor.core.publisher.Mono;

/**
 * Runs the pixel analysis. Program reads a file of URLs to image files,
 * fetches the Images and determines the three most common pixels.
 * The results are written to a file "pixels.txt".
 *
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 * @since 1.8
 */
public class Main {

    // File contains list of URLs of image files
    private static final String IMAGE_LIST_URL =
        "https://gist.githubusercontent.com/ehmo/e736c827ca73d84581d812b3a27bb132/raw/77680b283d7db4e7447dbf8903731bb63bf43258/input.txt";

    /**
     * Launches the application. Also displays the time elapsed.
     *
     * @param args not used
     *
     * @throws IOException if the file containing the image URLs cannot
     *     be read. This would presumably only happen if the host server
     *     were down or if the program is running on a machine without
     *     an Internet connection. In either case, recovery is
     *     impossible, so the program just exits.
     */
    public static void main(String... args) throws IOException {

        Instant start = Instant.now();

        //assemble the Mono
        ImagesAnalysis imagesAnalysis = new ImagesAnalysis(
                IMAGE_LIST_URL, "pixels.txt");
        Mono<Path> mono = imagesAnalysis.get();

        Instant assemblyDone = Instant.now();
        Duration duration = Duration.between(start, assemblyDone);
        System.out.println("Assembly complete in " +
                duration.toMillis() + " ms. Starting to Process");

        /*
         * It takes little time (a second or so) to get to this  point.
         * No real processing occurs until Mono.block() or
         * Flux.subscribe() are called. The next line takes a few
         * minutes to run.
         */
        mono.block();

        duration = Duration.between(assemblyDone, Instant.now());
        System.out.println("Executiion complete in " +
                (duration.toMillis() / 1000) + " seconds.");
    }

}
