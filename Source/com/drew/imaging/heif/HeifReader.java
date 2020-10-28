/*
 * Copyright 2002-2019 Drew Noakes and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package com.drew.imaging.heif;

import com.drew.lang.StreamReader;
import com.drew.metadata.heif.boxes.Box;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HeifReader
{
    public void extract(InputStream inputStream, HeifHandler<?> handler)
    {
        StreamReader reader = new StreamReader(inputStream);
        reader.setMotorolaByteOrder(true);
        processBoxes(reader, -1, handler);        
    }
    
    /**
     * Reads Exif and ICC profile bytes from the given stream and combines to a single byte array 
     * @param inputStream a stream from which the file data may be read.  The stream must be positioned at the
     *                    beginning of the file's data.
     * @param handler the handler class instance .     
     * @return byte array of Exif and ICC profile data.     
     */
    public byte[] readBytes(InputStream inputStream, HeifHandler<?> handler)
    {
    	byte[] dataBytes = null;
    	try
    	{
	        StreamReader reader = new StreamReader(inputStream);
	        reader.setMotorolaByteOrder(true);
	        getExifAndICCProfileStream(reader, -1, handler);        
	        ArrayList<byte[]> exifData = handler.metadata.heifExifBytes;
	        ArrayList<byte[]> iccData = handler.metadata.heifICCBytes;
	        
	        int arraySize = 0;
	        for (byte[] exifBytes : exifData) 
	        { 
	        	arraySize += exifBytes.length;
	        }
	        
	        for (byte[] iccBytes : iccData) 
	        { 
	        	arraySize += iccBytes.length;
	        }
	        
	        if(arraySize >0)
	        {
	        	int bytesCopied = 0;
	        	if(exifData != null)
	        	{
	        		arraySize = arraySize - (exifData.size() * 10);	        		
	        	}
	        	
	        	dataBytes = new byte[arraySize];
	        	
	        	for (byte[] exifBytes : exifData) 
	            { 	
	        		System.arraycopy(exifBytes, 10, dataBytes, bytesCopied, exifBytes.length-10);
	        		bytesCopied += exifBytes.length - 10;	        		
	            }
	        	
	        	for (byte[] iccBytes : iccData) 
	            { 	        		
	        		System.arraycopy(iccBytes, 0, dataBytes, bytesCopied, iccBytes.length);
	        		bytesCopied += iccBytes.length;
	            }
	        }
	        else
	        {
	        	 return null;
	        }
    	}
    	catch(Exception ex)
    	{
    		System.out.println(ex);
    	}
    	
        return dataBytes;
    }

    private void processBoxes(StreamReader reader, long atomEnd, HeifHandler<?> handler)
    {
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                Box box = new Box(reader);

                // Determine if fourCC is container/atom and process accordingly
                // Unknown atoms will be skipped

                if (handler.shouldAcceptContainer(box)) {
                    handler.processContainer(box, reader);
                    processBoxes(reader, box.size + reader.getPosition() - 8, handler);
                } else if (handler.shouldAcceptBox(box)) {
                    handler = handler.processBox(box, reader.getBytes((int)box.size - 8));
                } else if (box.size > 1) {
                    reader.skip(box.size - 8);
                } else if (box.size == -1) {
                    break;
                }
            }
        } catch (IOException e) {
            // Currently, reader relies on IOException to end
        }
    }
    
    /**
     * Iterates through the given stream reader to fetch required bytes 
     * @param atomEnd represents the current index
     * @param handler the handler class instance .    
     */
    private void getExifAndICCProfileStream(StreamReader reader, long atomEnd, HeifHandler<?> handler)
    {
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                Box box = new Box(reader);

                // Determine if fourCC is container/atom and process accordingly
                // Unknown atoms will be skipped

                if (handler.shouldAcceptContainer(box)) {
                    handler.processContainerToReadBytes(box, reader);
                    getExifAndICCProfileStream(reader, box.size + reader.getPosition() - 8, handler);
                } else if (handler.shouldAcceptBox(box)) {
                    handler = handler.processBoxToReadBytes(box, reader.getBytes((int)box.size - 8));
                } else if (box.size > 1) {
                    reader.skip(box.size - 8);
                } else if (box.size == -1) {
                    break;
                }
            }
        } catch (IOException e) {
            // Currently, reader relies on IOException to end
        } 
    }
}
