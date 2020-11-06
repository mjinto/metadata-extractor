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

import com.drew.lang.SequentialReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.StreamReader;
import com.drew.metadata.heif.HeifBoxTypes;
import com.drew.metadata.heif.HeifContainerTypes;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.boxes.Box;
import com.drew.metadata.icc.IccDirectory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HeifReader {

    /**
     * Map to hold file type and its metadata
     */
    private static final Set<String> ACCEPTABLE_PRE_META_BOX_TYPES = new HashSet<String>(
            Arrays.asList(HeifBoxTypes.BOX_FILE_TYPE, HeifContainerTypes.BOX_METADATA));

    /**
     * Iterates through the input stream to find the meta box
     * 
     * @param inputStream a stream from which the file data may be read. The stream
     *                    must be positioned at the beginning of the file's data.
     * @param handler     the handler class instance .
     * @return Map with byte array of Exif as key and display p3 status as value.
     */
    public void extract(InputStream inputStream, HeifHandler<?> handler) throws ImageProcessingException {
        // We need to read through the input stream to find the meta box which will tell
        // us what handler to use

        // The meta box is not necessarily the first box, so we need to mark the input
        // stream (if we can)
        // so we can re-read the stream with the proper handler if necessary

        try {
            boolean markSupported = false;
            if (inputStream.markSupported()) {
                markSupported = true;
                inputStream.mark(inputStream.available() + 1); // +1 since we're going to read past the end of the
                                                               // stream by 1 byte
            }

            StreamReader reader = new StreamReader(inputStream);
            reader.setMotorolaByteOrder(true);

            processTopLevelBoxes(inputStream, reader, -1, handler, markSupported, false);
        } catch (IOException e) {
            // Any errors should have been added to the directory
        }
    }

    /**
     * Processes the top level boxes
     * 
     * @param inputStream   a stream from which the file data may be read. The
     *                      stream must be positioned at the beginning of the file's
     *                      data.
     * @param reader        the sequential reader class instance .
     * @param atomEnd       the index of reader
     * @param handler       the handler class instance
     * @param markSupported the flag of mark support
     * @param exifSupported the flag of exif support
     * @return Map with byte array of Exif as key and display p3 status as value.
     */
    private void processTopLevelBoxes(InputStream inputStream, SequentialReader reader, long atomEnd,
            HeifHandler<?> handler, boolean markSupported, boolean exifSupported)
            throws ImageProcessingException, IOException {
        boolean foundMetaBox = false;
        boolean needToReset = false;
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                Box box = new Box(reader);

                if (!foundMetaBox && !ACCEPTABLE_PRE_META_BOX_TYPES.contains(box.type)) {
                    // If we hit a box that needs a more specific handler (like mdat) without yet
                    // hitting the meta box,
                    // we'll need to reset the stream and use the correct handler once we find it
                    needToReset = true;
                }

                if (HeifContainerTypes.BOX_METADATA.equalsIgnoreCase(box.type)) {
                    foundMetaBox = true;
                }

                handler = processBox(reader, box, handler, exifSupported);
            }
        } catch (IOException e) {
            // Currently, reader relies on IOException to end
        }

        if (needToReset && markSupported) {
            inputStream.reset();
            reader = new StreamReader(inputStream);
            processBoxes(reader, -1, handler, exifSupported);
        } else if (needToReset) {
            HeifDirectory heifDirectory = handler.metadata.getFirstDirectoryOfType(HeifDirectory.class);
            if (heifDirectory != null) {
                heifDirectory.addError(
                        "Unable to extract Exif data because inputStream was not resettable and 'meta' was not first box");
            }
        }
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
            boolean markSupported = false;
            if (inputStream.markSupported()) {
                markSupported = true;
                inputStream.mark(inputStream.available() + 1); // +1 since we're going to read past the end of the
                                                               // stream by 1 byte
            }

            StreamReader reader = new StreamReader(inputStream);
            reader.setMotorolaByteOrder(true);
            processTopLevelBoxes(inputStream, reader, -1, handler, markSupported, true);
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

    /**
     * Iterates through the reader to process the box
     * 
     * @param reader        the sequential reader class instance .
     * @param atomEnd       the index of reader
     * @param handler       the handler class instance
     * @param exifSupported the flag of exif support
     * @return data bytes of Exif information.
     */
    private HeifHandler<?> processBoxes(SequentialReader reader, long atomEnd, HeifHandler<?> handler,
            boolean exifSupported) {
        try {
            while (atomEnd == -1 || reader.getPosition() < atomEnd) {

                Box box = new Box(reader);

                handler = processBox(reader, box, handler, exifSupported);
            }
        } catch (IOException e) {
            // Currently, reader relies on IOException to end
        }
        return handler;
    }

    /**
     * Process the available boxes
     * 
     * @param reader        the sequential reader class instance .
     * @param atomEnd       the index of reader
     * @param handler       the handler class instance
     * @param exifSupported the flag of exif support
     * @return data bytes of Exif information.
     */
    private HeifHandler<?> processBox(SequentialReader reader, Box box, HeifHandler<?> handler, boolean exifSupported)
            throws IOException {
        if (handler.shouldAcceptContainer(box)) {
            if (exifSupported) {
                handler.processContainerToReadBytes(box, reader);
            } else {
                handler.processContainer(box, reader);
            }
            handler = processBoxes(reader, box.size + reader.getPosition() - 8, handler, exifSupported);
        } else if (handler.shouldAcceptBox(box)) {
            handler = handler.processBox(box, reader.getBytes((int) box.size - 8));
        } else if (box.size > 1) {
            reader.skip(box.size - 8);
        }
        return handler;
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
                isDisplayP3 = currentDirectory.getDescription(IccDirectory.TAG_PROFILE_DESCRIPTION).trim().toLowerCase()
                        .equals("display p3")
                        && currentDirectory.getString(IccDirectory.TAG_COLOR_SPACE).trim().toLowerCase().equals("rgb")
                        && currentDirectory.getString(IccDirectory.TAG_PROFILE_CONNECTION_SPACE).trim().toLowerCase()
                                .equals("xyz")
                        && currentDirectory.getString(IccDirectory.TAG_XYZ_VALUES).trim().toLowerCase()
                                .equals("0.9642 1 0.82491");
            }
        } catch (Exception ex) {
            throw new ImageProcessingException("Failed to process DisplayP3 data. " + ex.getMessage());
        }

        return isDisplayP3;
    }
}
