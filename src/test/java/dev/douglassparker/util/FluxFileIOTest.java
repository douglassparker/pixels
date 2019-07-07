package dev.douglassparker.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.function.Supplier;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Tests for {@link FluxFileIO}.
 *
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 */
@SuppressWarnings("static-method")
public class FluxFileIOTest {

    /**
     * Confirms that the Supplier can supply a {@link BufferedReader}.
     *
     * @throws IOException on a test failure.
     */
    @Test
    public void testChannelSupplier() throws IOException {
        String fileName = "foo.txt";
        Supplier<BufferedWriter> supplier =
            FluxFileIO.supplyWriter(fileName);
        try(BufferedWriter writer = supplier.get()) {
            assertThat(writer, is(notNullValue()));
        } finally {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Confirms that the accumulator correctly writes lines to the
     * writer.
     *
     * @throws IOException on an unexpected test failure. Since the
     *     test uses a {@link StringWriter}, this should never happen.
     */
    @Test
    public void testAccumulator() throws IOException {
        String[] words = {"aaa", "bbb", "ccc" };
        try(StringWriter sw = new StringWriter();
            BufferedWriter bw = new BufferedWriter(sw);) {
            Flux.fromArray(words)
                .reduceWith(() -> bw, FluxFileIO.ACCUMULATOR)
                .subscribe();
            bw.flush();
            String[] lines = sw.getBuffer().toString()
                    .split(System.lineSeparator());
            assertThat(lines, is(notNullValue()));
            assertThat(lines, is(arrayWithSize(3)));
            assertThat(lines, is(arrayContaining(words)));
        }
    }


    /**
     * Confirms CLOSE actually closes the Writer.
     * writer.
     *
     * @throws IOException on an unexpected test failure. Since the
     *     test uses a {@link StringWriter}, this should never happen.
     */
    @Test
    public void testClose() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        bw.write("abc");
        FluxFileIO.CLOSE.accept(bw, null);
        try {
            bw.write("def");
            fail("Able to write to closed writer");
        } catch (@SuppressWarnings("unused") IOException e) {
            // expected
        }

    }

    /**
     * Tests the {@link FluxFileIO#getChannelReader(URL)} method.
     * Creates a non-blocking Reader for a tests file, reads it, and
     * verifies the results.
     *
     * @throws IOException if the reader cannot be created and read.
     */
    @Test
    public void testGetChannelReader() throws IOException {
        URL url = FluxFileIOTest.class.getClassLoader()
            .getResource("in.txt");
        try (Reader reader = FluxFileIO.getChannelReader(url);
                BufferedReader buff = new BufferedReader(reader)) {
            assertThat(reader, is(notNullValue()));
            assertThat(buff, is(notNullValue()));
            assertThat(buff.readLine(), is("aaa"));
            assertThat(buff.readLine(), is("bbb"));
            assertThat(buff.readLine(), is("ccc"));
            assertThat(buff.readLine(), is(nullValue()));
        }
    }

    /**
     * Tests the {@link FluxFileIO#getReaderFlux(Reader)} method.
     * Creates a Flux form a StringReader, reads it, and verifies
     * the results.
     */
    @Test
    public void testGetReaderFlux() {
        StringReader reader = new StringReader("aaa\nbbb\nccc");
        Flux<String> flux = FluxFileIO.getReaderFlux(reader);
        StepVerifier.create(flux)
            .expectNext("aaa")
            .expectNext("bbb")
            .expectNext("ccc")
            .verifyComplete();
    }

}
