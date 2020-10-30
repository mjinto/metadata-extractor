package com.drew.imaging.heif;

import org.junit.Test;

import com.drew.imaging.ImageMetadataReader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class MetadataExtractorTest {
	/**
     * Validates EXIF , ICC Profile data bytes of a HEIF image.              
     */
	@Test
    public void testExifAndICCData_HEIFValidImage()
    {
		try
		{
			String path = "Tests\\Data\\test_image.HEIC";
			 
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();			
			byte[] data = ImageMetadataReader.readExifAndICCBytes(absolutePath);
			byte[] comaparisonData = getExifICCDataToCompare();		
			
			assertEquals(comaparisonData.length, data.length);
			assertArrayEquals(comaparisonData,data);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
    }

	/**
     * Validates EXIF , ICC Profile data bytes of a JPEG image.              
     */
	@Test
    public void testExifAndICCData_JPEGValidImage()
    {
		try
		{
			String path = "Tests\\Data\\test_image.jpg";
			 
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();			
			byte[] data = ImageMetadataReader.readExifAndICCBytes(absolutePath);
			assertNull(data);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
    }
	
	/**
     * Validates EXIF , ICC Profile data bytes of a PNG image.              
     */
	@Test
    public void testExifAndICCData_PNGGValidImage()
    {
		try
		{
			String path = "Tests\\Data\\test_image.png";
			 
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();			
			byte[] data = ImageMetadataReader.readExifAndICCBytes(absolutePath);
			assertNull(data);			
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
    }
	
	/**
     * Validates EXIF , ICC Profile data bytes of a HEIF image with no EXIF data in it.              
     */
	@Test
    public void testExifAndICCData_HEIFImageWithoutEXIF()
    {
		try
		{
			String path = "Tests\\Data\\test_Image_NoExifData.heic";
			 
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();			
			byte[] data = ImageMetadataReader.readExifAndICCBytes(absolutePath);
			assertNull(data);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
    }
	
	/**
     * Validates EXIF , ICC Profile data bytes of a non existing image.              
     */
	@Test
    public void testExifAndICCData_NonExistingImage()
    {
		try
		{
			String path = "Tests\\Data\\test_Image_NoImage.heic";
			 
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();			
			byte[] data = ImageMetadataReader.readExifAndICCBytes(absolutePath);
			assertNull(data);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
    }
	
	/**
     * Validates EXIF , ICC Profile data bytes of an invalid HEIF image.              
     */
	@Test
    public void testExifAndICCData_InvalidImage()
    {
		try
		{
			String path = "Tests\\Data\\test_Image_2.heic";
			 
			File file = new File(path);
			String absolutePath = file.getAbsolutePath();			
			byte[] data = ImageMetadataReader.readExifAndICCBytes(absolutePath);
			byte[] comaparisonData = getExifICCDataToCompare();				
			assertNotEquals(data, comaparisonData);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
    }
	
	/**
     * Writes extracted Exif and ICC profile bytes to the given path.
     * @param data bytes which is to be saved.
     * @param path at which the data file is to be saved.          
     */
	public static void writeByte(byte[] bytes, String path)
    {
        try {

        	String output = path + "\\OutputFile";
            File file = new File(output);
            // Initialize a pointer
            // in file using OutputStream
            FileOutputStream os = new FileOutputStream(file,true);

            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully"
                    + " byte inserted");

            // Close the file
            os.close();
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

	/**
     * Reads Exif and ICC profile bytes from the file which is kept at Data folder as a data reference.     
     * @return byte array of Exif and ICC profile data.     
     */
	public static byte[] getExifICCDataToCompare()
	{
		byte[] bytes = null;
		try
		{
			String filePath = "Tests\\Data\\ExifICCData";	
			File file = new File(filePath);
			bytes = Files.readAllBytes(file.toPath());
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		
		return bytes;
	}
}
