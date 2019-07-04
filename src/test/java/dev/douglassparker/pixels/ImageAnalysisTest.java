package dev.douglassparker.pixels;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import reactor.core.publisher.Flux;

/**
 * Tests for <code>ImageAnalysisClass</code>
 * 
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 */
@SuppressWarnings("static-method")
public class ImageAnalysisTest extends BaseImageAnalysisTest {

	/**
	 * Confirms that an image can be retrieved from a URL.
	 */
	@Test
	public void testFetchImage() {
		BufferedImage image = imageAnalysis.fetchImage(testUrl);
		assertThat(image, is(notNullValue()));
	}

	/**
	 * Confirms the logic to extract the pixels from an image.
	 */
	@Test
	public void testGetImagePixelFlux() {
		List<Integer> pixels = imageAnalysis.getImagePixelFlux(
				imageAnalysis.fetchImage(testUrl))
			.collect(Collectors.toList()).block();
		assertThat(pixels, is(notNullValue()));	
		// test image has size 800 by 600.
		assertThat(pixels.size(), is(800 * 600));
	}

	/**
	 * Confirms the logic to transform an integer into a six character
	 * String.
	 */
	@Test
	public void testToRGB() {
		assertThat(imageAnalysis.toRGB(0),is("000000"));	
		assertThat(imageAnalysis.toRGB(-1),is("ffffff"));	
		assertThat(imageAnalysis.toRGB(0xcafebabe),is("febabe"));		
	}	

	/**
	 * Confirms the ability to extract the three most occurrences in
	 * from a Stream of Strings.
	 */
	@Test
	public void testGetTop3() {
		Stream<String> stream = Stream.of("BEADED", "DECADE", "DEFACE"
				,"FACADE", "DECADE","DEFACE", "FACADE", "DEFACE"
				,"FACADE", "FACADE");
		Map<String,Long> map = Flux.fromStream(stream)
			.collect(Collectors.groupingBy(
				Function.identity(), Collectors.counting()))
			.block();
		String[] top3 = imageAnalysis.getTop3(map);
		assertThat(top3, is(notNullValue()));
		assertThat(top3.length, is(3));
		assertThat(top3[0], is("FACADE"));
		assertThat(top3[1], is("DEFACE"));
		assertThat(top3[2], is("DECADE"));		
	}	

	/** 
	 * Tests that the apply() method correctly identifies the three
	 * most occurring colors in the test.jpg file as black, white, and
	 * red.  
	 */
	@Test
	public void testApply() {
		String s = imageAnalysis.apply(TEST_IMAGE).block();
		assertThat(s, is("test.jpg,#000000,#FFFFFF,#FE0000"));
	}

}
