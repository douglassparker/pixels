package dev.douglassparker.pixels;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Analyzes a single image for its three most common pixels.
 *
 * @author <a href="mailto:douglassparker@me.com">Douglass Parker</a>
 * @since 1.8
 */
class ImageAnalysis implements Function<String, Mono<String>> {

    /**
     * Included in the output message when no image can be found at the
     * provided URL.
     */
    static String ERROR_SUFFIX = " - NO IMAGE AT THIS LOCATION";

    /**
     * Returns pixel information as for an image.
     *
     * @param loc the location of the image, as a URL string.
     * @return a Mono of a String. The String is the input loc with
     *     the three most common pixels RGB values appended.
     */
    @Override
    public Mono<String> apply(String loc) {
        /*
         *  1. Create a Mono.
         *  2. Transform the location to a URL.
         *  3. Read the URL into a BufferedImage Object.
         *  4. Return the the image pixels as a Flux<Integer>.
         *  5. Transform each integer to a 6 character RGB String.
         *  6. Collect a map with the keys being the RGB String and the
         *     values being a count of how many times they occur.
         *  7. Return the three keys with the highest values.
         *  8. Format the response String.
         *  9. Return an error String if the URL is invalid,
         *     unreachable, or not an image.
         * 10. Log the events.
         */
        return Mono.just(loc)                                     // 1
            .map(this::getURL)                                    // 2
            .map(this::fetchImage)                                // 3
            .flatMapMany(this::getImagePixelFlux)                 // 4
            .map(this::toRGB)                                     // 5
            .collect(Collectors.groupingBy(                       // 6
                    Function.identity(), Collectors.counting()))
            .map(this::getTop3)                                   // 7
            .map(top3 -> {                                        // 8
                StringBuilder sb = new StringBuilder(loc);
                for (String s : top3) {
                    // In case image has only one or two colors,
                    if (s == null) {
                        break;
                    }
                    sb.append(",#").append(s.toUpperCase());
                }
                return sb.toString();
            })
            .onErrorReturn(loc + ERROR_SUFFIX)                    // 9
            .log();                                               // 10
    }

    /**
     * Returns the URL for a location. This is a separate method to
     * facilitate mocking in the tests.
     *
     *  @param loc a String in a valid URL format.
     *  @return a Uniform Resource Locator
     */
    URL getURL(String loc) {
        try {
            return new URL(loc);
        } catch (MalformedURLException e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Reads image from URL.
     *
     * @param loc the Web address of the Image.
     * @return an Image object
     * @see ImageIO#read(URL)
     */
    BufferedImage fetchImage(URL loc) {
        try {
            return ImageIO.read(loc);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Return the pixels in an image.
     *
     * @param image an Image with an accessible buffer of image data
     * @return a Flux of Integers, each of which encodes Alpha, Red,
     *     Green, Blue.
     * @see BufferedImage#getRGB(int, int, int, int, int[], int, int)
     * @see IntStream#of(int...)
     * @see IntStream#boxed()
     * @see Flux#fromStream(java.util.stream.Stream)
     */
    Flux<Integer> getImagePixelFlux(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(
                0, 0, width, height, null, 0, width);
        return Flux.fromStream(IntStream.of(pixels).boxed());
    }

    /**
     * Transforms an 32 bit ARGB Integer to a 6 character RGB
     * String.
     *
     * @param argb an Integer that encodes Alpha, Red, Green, Blue.
     * @return a String with RGB values as Hex characters, e.g.,
     *  "FE123C".
     */
    String toRGB(Integer argb) {
        String hex = Integer.toHexString(argb);

        /*
         * Hex string will not include leading zeroes. So left pad
         * it to length 8, then return the last 6 characters.
         */
        hex = "00000000".substring(hex.length()) + hex;
        return hex.substring(2);
    }

    /**
     * Returns the three pixel values with the highest count.
     *
     * @param map A Map with keys of six character RGB Strings
     *     representing pixels and values that are a count of how many
     *     times the pixels  appear in an image.
     *
     * @return the three most occurring pixels. If the image has only
     *     one or two colors, the returned array will still be of length
     *     3, but will contain nulls.
     */
    String[] getTop3(Map<String, Long> map) {
        /*
         * A less verbose implementation would sort the map entries.
         * However, a good sorting algorithm would be O(N*logN). This
         * routine runs in linear time (O(N)).
         */
        String[] top3Pixels = {null, null, null};
        long[] top3Count = {0L, 0L, 0L};

        for (Map.Entry<String, Long> entry : map.entrySet()) {
            String key = entry.getKey();
            long value = entry.getValue();
            if (value > top3Count[0]) {
                top3Count[2] = top3Count[1];
                top3Count[1] = top3Count[0];
                top3Count[0] = value;
                top3Pixels[2] = top3Pixels[1];
                top3Pixels[1] = top3Pixels[0];
                top3Pixels[0] = key;
            } else if (value > top3Count[1]) {
                top3Count[2] = top3Count[1];
                top3Count[1] = value;
                top3Pixels[2] = top3Pixels[1];
                top3Pixels[1] = key;
            }  else if (value > top3Count[1]) {
                top3Count[2] = value;
                top3Pixels[2] = key;
            }
        }

         return top3Pixels;
    }

}
