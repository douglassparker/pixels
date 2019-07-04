package dev.douglassparker.pixels;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests processing of multiple images.
 * 
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 * @since 1.8
 */
@SuppressWarnings("static-method")
public class ImagesAnalysisTest extends BaseImageAnalysisTest {

	/**
	 * Verifies that input is successfully processed, Two elements are
	 * tested. The first is a test image file. The second is an invalid
	 * URL. The results are written to a temporary output file. 
	 * 
	 * @throws IOException if the output file cannot be read back in
	 * 	and verified.
	 */
	@Test
	public void testApply() throws IOException {
		String s = TEST_IMAGE + "\nhttp://noimagehere";
		Reader rdr = new StringReader(s);

		ImagesAnalysis imagesAnalysis = spy(new ImagesAnalysis(
				null, "test-pixels.txt", imageAnalysis));
		doReturn(rdr).when(imagesAnalysis).getReader();
		
		Mono<Path> mono = imagesAnalysis.get();
		assertThat(mono, is(notNullValue()));		
		Path path = mono.block();
		assertThat(path, is(notNullValue()));
		
		try (BufferedReader reader = Files.newBufferedReader(
				path, StandardCharsets.UTF_8)) {
			Flux<String> flux = Flux.fromStream(reader.lines());
			StepVerifier.create(flux)
				.expectNext("test.jpg,#000000,#FFFFFF,#FE0000")
				.expectNext("http://noimagehere" + 
						ImageAnalysis.ERROR_SUFFIX)
				.verifyComplete();
		} finally {		
			File file = path.toFile();
			if (file.exists()) {
				file.delete();
			}
		}
	}

}
