package io.mio.compress;

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
public class GzipCompress implements Compress {

    @Override
    public byte[] compress(byte[] data) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out);) {
            gzip.write(data);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] uncompress(byte[] data) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayInputStream in = new ByteArrayInputStream(data);
             GZIPInputStream unGzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[2048];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }

            return out.toByteArray();
        }
    }

}
