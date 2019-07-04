package dev.douglassparker.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.BaseStream;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides support for reading and writing a Reactor {@link Flux} to a 
 * File. The class uses java nio (non-blocking io) Channels. 
 * <p>
 * <strong>Usage: Writing to a file</strong><br><br>
 * Given a String Flux, and an output file name, code something like 
 * this:
 * <pre>{@code
 *	Mono<Path> pathMono = flux.publishOn(Schedulers.elastic()) 
 *		.reduceWith(FluxFileIO.supplyWriter(fileName), FluxFileIO.ACCUMULATOR)
 *		.doOnSuccessOrError(FluxFileIO.CLOSE)
 *		.map(writer -> FileSystems.getDefault().getPath(outFileName));   
 * }</pre>
 * <p>
 * <strong>Usage: Reading from a file</strong><br>
 * <pre>{@code
 * Reader reader = FluxFileIO.getChannelReader(<URL string>);
 * Flux<String> = FluxFileIO.getReaderFlux(reader);  
 * }</pre>
 *  
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 * @see Channels
 */
public class FluxFileIO {

	/**
	 * Supplier for a {@link BufferedWriter} that uses non-blocking IO. 
	 */
	static class ChannelWriterSupplier
			implements Supplier<BufferedWriter> {
	
		private String fileName;
		
		ChannelWriterSupplier(String fileName) {
			this.fileName = fileName;
		}

		/**
		 * Returns a Writer.
		 * 
		 * @return A BufferedWriter that wraps a Channel.
		 * 
		 * @see FileOutputStream#getChannel()
		 * @see Channels#newWriter(java.nio.channels.WritableByteChannel, String)
		 */
		@SuppressWarnings("resource")
		@Override
		public BufferedWriter get() {
			try {
				Writer writer = Channels.newWriter(
						new FileOutputStream(fileName).getChannel(), 
						StandardCharsets.UTF_8.toString());
				return new BufferedWriter(writer);
			} catch (FileNotFoundException e) {
				throw Exceptions.propagate(e);
			}
		}

	}

	/**
	 * Creates and returns a writer supplier.
	 * 
	 * @param fileName the output file name.
	 * @return a BufferedWriter supplier.
	 * @see Flux#reduceWith(Supplier, BiFunction)
	 */
	public static Supplier<BufferedWriter> supplyWriter(
			String fileName) {
		return new ChannelWriterSupplier(fileName);		
	}

	/**
	 * An accumulator that writes a single line to a Channel.
	 * 
	 * @see Flux#reduceWith(Supplier, BiFunction)
	 */
	public static final BiFunction
			<BufferedWriter, String, BufferedWriter> 
	ACCUMULATOR = (out, line) -> {
		try {
			out.write(line);
			out.newLine();
			return out;
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
		
	};

	/**
	 * The "finally" logic that closes the writer.
	 * 
	 * @see Mono#doOnSuccessOrError(BiConsumer) 
	 */
	public static final BiConsumer<BufferedWriter, Throwable>
	CLOSE = (out, t) -> {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				throw Exceptions.propagate(e);
			}
		}		
	};

	/**
	 * Gets a non-blocking reader that wraps a 
	 * 	{@link ReadableByteChannel}.
	 *  
	 * @param url the location of the input. May not be null.
	 * @return a Reader
	 * @see URL#openConnection()
	 * @see URLConnection#getInputStream()
	 * @see Channels#newChannel(InputStream)
	 * @see Channels#newReader(ReadableByteChannel, String)
	 * @see StandardCharsets
	 */
	@SuppressWarnings("resource")
	public static final Reader getChannelReader(URL url) {
		try {
			InputStream is = url.openConnection().getInputStream();
			ReadableByteChannel channel = Channels.newChannel(is);
			return Channels.newReader(channel, 
					StandardCharsets.UTF_8.toString());	
		} catch(IOException e) {
			throw Exceptions.propagate(e);
		}
	}

	/**
	 * Gets a Flux for a Reader.
	 * 
	 * @param reader a Reader. The Reader will be automatically closed
	 * 	once the flux is exhausted.
	 * @return a Flux of Strings. Each element in the Flux is a line
	 * 	from the Reader.
	 * @see Flux#using(java.util.concurrent.Callable, java.util.function.Function, java.util.function.Consumer)
	 */
	public static final Flux<String> getReaderFlux(Reader reader) {
		return Flux.using(
			() -> new BufferedReader(reader).lines(),
			Flux::fromStream, 
			BaseStream::close);		
	}
	
}
