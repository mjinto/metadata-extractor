package com.drew.imaging.heif;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MetadataExtractorTest {
	/**
	 * Validates EXIF , ICC Profile data bytes of a HEIF image.
	 */
	@Test
	public void testExifAndICCData_HEIFValidImage() {
		try {
			String path = "Tests\\Data\\test_image.HEIC";
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();
			HashMap<byte[], Boolean> data = HeifMetadataReader.getExifAndDisplayP3Info(getInputStream(absolutePath));
			Map.Entry<byte[], Boolean> entry = data.entrySet().iterator().next();
			byte[] exifData = entry.getKey();
			Boolean isDisplayP3 = entry.getValue();
			byte[] comaparisonData = getExifICCDataToCompare();

			assertEquals(comaparisonData.length - 10, exifData.length);
			assertTrue(isDisplayP3);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Validates EXIF , ICC Profile data bytes of a JPEG image.
	 */
	@Test
	public void testExifAndICCData_JPEGValidImage() {
		try {
			String path = "Tests\\Data\\test_image.jpg";
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();
			HashMap<byte[], Boolean> data = HeifMetadataReader.getExifAndDisplayP3Info(getInputStream(absolutePath));
			Map.Entry<byte[], Boolean> entry = data.entrySet().iterator().next();
			byte[] exifData = entry.getKey();
			Boolean isDisplayP3 = entry.getValue();

			assertNull(exifData);
			assertFalse(isDisplayP3);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Validates EXIF , ICC Profile data bytes of a PNG image.
	 */
	@Test
	public void testExifAndICCData_PNGGValidImage() {
		try {
			String path = "Tests\\Data\\test_image.png";
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();
			HashMap<byte[], Boolean> data = HeifMetadataReader.getExifAndDisplayP3Info(getInputStream(absolutePath));
			Map.Entry<byte[], Boolean> entry = data.entrySet().iterator().next();
			byte[] exifData = entry.getKey();
			Boolean isDisplayP3 = entry.getValue();

			assertNull(exifData);
			assertFalse(isDisplayP3);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Validates EXIF , ICC Profile data bytes of a HEIF image with no EXIF data in
	 * it.
	 */
	@Test
	public void testExifAndICCData_HEIFImageWithoutEXIF() {
		try {
			String path = "Tests\\Data\\test_Image_NoExifData.heic";
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();
			HashMap<byte[], Boolean> data = HeifMetadataReader.getExifAndDisplayP3Info(getInputStream(absolutePath));
			Map.Entry<byte[], Boolean> entry = data.entrySet().iterator().next();
			byte[] exifData = entry.getKey();
			Boolean isDisplayP3 = entry.getValue();

			assertNull(exifData);
			assertFalse(isDisplayP3);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Validates EXIF , ICC Profile data bytes of a non existing image.
	 */
	@Test
	public void testExifAndICCData_NonExistingImage() {
		try {
			String path = "Tests\\Data\\test_Image_NoImage.heic";
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();
			HashMap<byte[], Boolean> data = HeifMetadataReader.getExifAndDisplayP3Info(getInputStream(absolutePath));
			Map.Entry<byte[], Boolean> entry = data.entrySet().iterator().next();
			byte[] exifData = entry.getKey();
			Boolean isDisplayP3 = entry.getValue();

			assertNull(exifData);
			assertFalse(isDisplayP3);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Validates EXIF , ICC Profile data bytes of an invalid HEIF image.
	 */
	@Test
	public void testExifAndICCData_InvalidImage() {
		try {
			String path = "Tests\\Data\\test_Image_2.heic";
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();
			HashMap<byte[], Boolean> data = HeifMetadataReader.getExifAndDisplayP3Info(getInputStream(absolutePath));
			byte[] comaparisonData = getExifICCDataToCompare();
			Map.Entry<byte[], Boolean> entry = data.entrySet().iterator().next();
			byte[] exifData = entry.getKey();
			Boolean isDisplayP3 = entry.getValue();

			assertNotEquals(exifData, comaparisonData);
			assertFalse(isDisplayP3);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	/**
	 * Writes extracted Exif and ICC profile bytes to the given path.
	 * 
	 * @param data bytes which is to be saved.
	 * @param path at which the data file is to be saved.
	 */
	public static void writeByte(byte[] bytes, String path) {
		try {

			String output = path + "\\OutputFile";
			File file = new File(output);
			// Initialize a pointer
			// in file using OutputStream
			FileOutputStream os = new FileOutputStream(file, true);

			// Starts writing the bytes in it
			os.write(bytes);
			System.out.println("Successfully" + " byte inserted");

			// Close the file
			os.close();
		}

		catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

	/**
	 * Reads Exif and ICC profile bytes from the file which is kept at Data folder
	 * as a data reference.
	 * 
	 * @return byte array of Exif and ICC profile data.
	 */
	private static byte[] getExifICCDataToCompare() {
		byte[] bytes = null;
		try {
			String filePath = "Tests\\Data\\Exif_Only";
			File file = new File(filePath);
			bytes = Files.readAllBytes(file.toPath());
		} catch (Exception ex) {
			System.out.println(ex);
		}

		return bytes;
	}

	/**
	 * Returns the input stream of an image at the given path
	 *
	 * @param filePath represents the path at which the image is available
	 */
	private BufferedInputStream getInputStream(String filePath) {
		BufferedInputStream bufferedInputStream = null;
		try {
			File file = new File(filePath);
			InputStream inputStream = new FileInputStream(file);
			;

			bufferedInputStream = inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream
					: new BufferedInputStream(inputStream);
		} catch (Exception ex) {
			System.out.println(ex);
		}

		return bufferedInputStream;
	}
}
