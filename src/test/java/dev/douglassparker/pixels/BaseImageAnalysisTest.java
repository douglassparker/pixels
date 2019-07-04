package dev.douglassparker.pixels;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.URL;

import org.junit.BeforeClass;

/**
 * Base class that provides a mocked <code>ImageAnalysis</code> instance 
 * for testing purposes. The <code>getURL()</code> method in that class
 * calls the {@link URL#URL(String)} constructor. While this works fine
 * for the http URLs the class normally processes, it does not work for
 * classpath URLs. To get a URL for a classpath resource, it is 
 * necessary to call {@link ClassLoader#getResource(String)}. The mock 
 * here is to return the URL for the "test.jpg" test image instead of 
 * throwing an exception.
 * 
 * @author  
 *  <a href="mailto:douglass.parker@centurylink.com">Douglass Parker</a>
 *
 */
public abstract class BaseImageAnalysisTest {
	
	/**
	 * The name of the test image. It is located in src/test/resources.
	 */	
	static final String TEST_IMAGE = "test.jpg";

	/**
	 * A partially mocked object that that analyzes an individual image. 
	 */
	static ImageAnalysis imageAnalysis;

	/**
	 * The URL for the test image.
	 */
	static URL testUrl;

	/**
	 * Sets up a partially mocked ImageAnalysis instance. 
	 */
	@BeforeClass
	public static void initClass() {
		testUrl = BaseImageAnalysisTest.class.getClassLoader()
				.getResource(TEST_IMAGE);
		imageAnalysis = spy(new ImageAnalysis());
		doReturn(testUrl).when(imageAnalysis).getURL(TEST_IMAGE);
	}	
	
}
