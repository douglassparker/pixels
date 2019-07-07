package dev.douglassparker.pixels;

import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.Supplier;

import dev.douglassparker.util.FluxFileIO;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Reads from the input file, processes the URLs, and writes to
 * the output file.
 *
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 * @since 1.8
 */
class ImagesAnalysis implements Supplier<Mono<Path>> {

    private final ImageAnalysis imageAnalysis;
    private String in;
    private String out;

    /**
     * Initialize with a (possibly mocked) ImageAnalysis object.
     * Useful for testing.
     *
     *  @param in The location of the input file. It must be in a valid
     *      URL format.
     *  @param out Name of the output file.
     *  @param imageAnalysis Object that processes an image to analyze
     *      the pixel count.
     */
    ImagesAnalysis(String in, String out, ImageAnalysis imageAnalysis) {
        this.in = in;
        this.out = out;
        this.imageAnalysis = imageAnalysis;
    }

    /**
     * Constructor that would normally be used. Equivalent to calling
     * <code>ImagesAnalysis(in, out, new ImageAnalysis())</code>.
     *
     *  @param in The location of the input file. It must be in a valid
     *      URL format.
     *  @param out Name of the output file.
     */
    ImagesAnalysis(String in, String out) {
        this(in, out, new ImageAnalysis());
    }

    /**
     * Creates file with pixel information based on the URLs supplied
     * by a reader.
     *
     * @return a Mono of a {@link Path}. The Path is the path to the
     *     output file. The caller must call <code>Mono.block()</code>
     *     to execute the analysis.
     * @see FluxFileIO
     */
    @Override
    public Mono<Path> get() {

        Path path = FileSystems.getDefault().getPath(out);

        /*
         * 1. Gets a Flux of String from the Input in a non-blocking
         *     manner. See FluxFileIO class for details.
         * 2. Each string is processed. See the ImageAnalysis
         *     class for more information.
         * 3. Multi-threading is easy with Reactor!
         * 4. The reduceWith method requires an initializer and an
         *     accumulator. See the FluxFileIO class for details.
         * 5. Closes the output file.
         * 6. Routes BufferedWriter to the output file.
         * 7. Logs the Reactor events to pixels.log.
         */
        return FluxFileIO.getReaderFlux(getReader()) // 1
            .flatMap(imageAnalysis)                  // 2
            .publishOn(Schedulers.elastic())         // 3
            .reduceWith(                             //4
                FluxFileIO.supplyWriter(out),
                FluxFileIO.ACCUMULATOR)
            .doOnSuccessOrError(FluxFileIO.CLOSE)    // 5
            .map(writer -> path)                     // 6
            .log();                                  // 7
    }

    /**
     * Returns a non-blocking reader. Broken out as a separate routine
     * to facilitate test mocking.
     *
     * @return a Reader that wraps a {@link ReadableByteChannel}
     * @see FluxFileIO#getChannelReader(URL)
     */
    Reader getReader() {
        try {
            return FluxFileIO.getChannelReader(new URL(in));
        } catch (MalformedURLException e) {
            throw Exceptions.propagate(e);
        }
    }

}
