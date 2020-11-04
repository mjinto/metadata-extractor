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
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifBoxHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HeifMetadataReader {
    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream) {
        Metadata metadata = new Metadata();
        new HeifReader().extract(inputStream, new HeifBoxHandler(metadata));
        return metadata;
    }

    /**
     * Reads Exif bytes from the input stream and also checks the Display P3 status.
     * 
     * @param inputStream a stream from which the file data may be read. The stream
     *                    must be positioned at the beginning of the file's data.
     * @return Map with byte array of Exif as key and display p3 status as value.
     */
    @NotNull
    public static HashMap<byte[], Boolean> getExifAndDisplayP3Info(@NotNull InputStream inputStream)
            throws ImageProcessingException, IOException {
        Metadata metadata = new Metadata();
        return new HeifReader().extractExifAndICCProfileStream(inputStream, new HeifBoxHandler(metadata));
    }
}
