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

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifBoxHandler;

import java.io.InputStream;
import java.util.ArrayList;

public class HeifMetadataReader
{
    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream)
    {
        Metadata metadata = new Metadata();
        new HeifReader().extract(inputStream, new HeifBoxHandler(metadata));
        return metadata;
    }
    
    /**
     * Reads Exif and ICC profile bytes from an {@link InputStream} using the handler instance.
     * @param inputStream a stream from which the file data may be read.  The stream must be positioned at the
     *                    beginning of the file's data.
     * @return byte array of Exif and ICC profile data.     
     */
    @NotNull
    public static byte[] readBytes(@NotNull InputStream inputStream)
    {
        Metadata metadata = new Metadata();
        byte[] data = new HeifReader().readBytes(inputStream, new HeifBoxHandler(metadata));
        return data;
    }
}
