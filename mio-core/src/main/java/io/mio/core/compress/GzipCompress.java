package io.mio.core.compress;

import io.mio.core.extension.Extension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The Data Compression Based on gzip.
 *
 * @author lry
 */
@Extension("gzip")
public class GzipCompress implements Compress {

    @Override
    public byte[] compress(byte[] data) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(data);
            }
            return out.toByteArray();
        }
    }

    @Override
    public byte[] uncompress(byte[] data) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
                try (GZIPInputStream inputStream = new GZIPInputStream(in)) {
                    byte[] buffer = new byte[2048];
                    int n;
                    while ((n = inputStream.read(buffer)) >= 0) {
                        out.write(buffer, 0, n);
                    }
                    return out.toByteArray();
                }
            }
        }
    }

}
