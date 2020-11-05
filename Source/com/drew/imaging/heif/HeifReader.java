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

import com.drew.imaging.ImageProcessingException;
import com.drew.lang.StreamReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.boxes.Box;
import com.drew.metadata.icc.IccDirectory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class HeifReader {

    public void extract(InputStream inputStream, HeifHandler<?> handler) {
        StreamReader reader = new StreamReader(inputStream);
        reader.setMotorolaByteOrder(true);
        processBoxes(reader, -1, handler);
    }

    /**
     * Reads Exif bytes from the input stream and also checks the Display P3 status.
     * 
     * @param inputStream a stream from which the file data may be read. The stream
     *                    must be positioned at the beginning of the file's data.
     * @param handler     the handler class instance .
     * @return Map with byte array of Exif as key and display p3 status as value.
     */
    public HashMap<byte[], Boolean> extractExifAndICCProfileStream(InputStream inputStream, HeifHandler<?> handler)
            throws ImageProcessingException, IOException {
        HashMap<byte[], Boolean> result = new HashMap<byte[], Boolean>();

        try {
            StreamReader reader = new StreamReader(inputStream);
            reader.setMotorolaByteOrder(true);
            getExifProfileStream(reader, -1, handler);
            result.put(getRequriedExifBytes(handler.metadata.heifExifBytes), ValidateDisplayP3Data(handler.metadata));
        } finally {

        }

        return result;
    }

    /**
     * Reads required EXIF bytes skipping the first 10 bytes from the full bytes.
     * 
     * @param exifData array list of exif data bytes extracted from the image.
     * @return data bytes of Exif information.
     */
    private byte[] getRequriedExifBytes(ArrayList<byte[]> exifData) throws ImageProcessingException {
        byte[] dataBytes = null;
        try {
            int arraySize = 0;

            for (byte[] exifBytes : exifData) {
                arraySize += exifBytes.length;
            }

            if (arraySize > 0) {
                int bytesCopied = 0;
                if (exifData != null) {
                    arraySize = arraySize - (exifData.size() * 10);
                }

                dataBytes = new byte[arraySize];

                for (byte[] exifBytes : exifData) {
                    System.arraycopy(exifBytes, 10, dataBytes, bytesCopied, exifBytes.length - 10);
                    bytesCopied += exifBytes.length - 10;
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new ImageProcessingException("Failed to process the extracted EXIF data bytes. " + ex.getMessage());
        }

        return dataBytes;
    }

    private void processBoxes(StreamReader reader, long atomEnd, HeifHandler<?> handler) {
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                Box box = new Box(reader);

                // Determine if fourCC is container/atom and process accordingly
                // Unknown atoms will be skipped

                if (handler.shouldAcceptContainer(box)) {
                    handler.processContainer(box, reader);
                    processBoxes(reader, box.size + reader.getPosition() - 8, handler);
                } else if (handler.shouldAcceptBox(box)) {
                    handler = handler.processBox(box, reader.getBytes((int) box.size - 8));
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
     * 
     * @param atomEnd represents the current index
     * @param handler the handler class instance .
     */
    private void getExifProfileStream(StreamReader reader, long atomEnd, HeifHandler<?> handler)
            throws ImageProcessingException {
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                Box box = new Box(reader);

                // Determine if fourCC is container/atom and process accordingly
                // Unknown atoms will be skipped

                if (handler.shouldAcceptContainer(box)) {
                    handler.processContainerToReadBytes(box, reader);
                    getExifProfileStream(reader, box.size + reader.getPosition() - 8, handler);
                } else if (handler.shouldAcceptBox(box)) {
                    handler = handler.processBox(box, reader.getBytes((int) box.size - 8));
                } else if (box.size > 1) {
                    reader.skip(box.size - 8);
                } else if (box.size == -1) {
                    break;
                }
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Validates icc profile tags
     * 
     * @param metadata represents the current metadata instance.
     */
    private Boolean ValidateDisplayP3Data(Metadata metadata) throws ImageProcessingException {
        Boolean isDisplayP3 = false;
        try {
            IccDirectory currentDirectory = metadata.getFirstDirectoryOfType(IccDirectory.class);
            if (currentDirectory != null) {
                isDisplayP3 = currentDirectory.getDescription(IccDirectory.TAG_PROFILE_DESCRIPTION).trim().toLowerCase().equals("display p3")
                        && currentDirectory.getString(IccDirectory.TAG_COLOR_SPACE).trim().toLowerCase().equals("rgb")
                        && currentDirectory.getString(IccDirectory.TAG_PROFILE_CONNECTION_SPACE).trim().toLowerCase().equals("xyz")
                        && currentDirectory.getString(IccDirectory.TAG_XYZ_VALUES).trim().toLowerCase().equals("0.9642 1 0.82491");                
            } 
        } catch (Exception ex) {
            throw new ImageProcessingException("Failed to process DisplayP3 data. " + ex.getMessage());
        }

        return isDisplayP3;
    }    
}
