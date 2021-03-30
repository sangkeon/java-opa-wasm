package io.github.sangkeon.opa.wasm;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.BufferedInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class BundleUtil {
    public static Bundle extractBundle(String filename) throws IOException {
        Bundle bundle = new Bundle();

        try (InputStream fi = Files.newInputStream(Paths.get(filename));
            InputStream bi = new BufferedInputStream(fi);
            InputStream gzi = new GzipCompressorInputStream(bi);
            ArchiveInputStream i = new TarArchiveInputStream(gzi)
        ) {
            ArchiveEntry entry = null;

            while ((entry = i.getNextEntry()) != null) {
                if (!i.canReadEntryData(entry)) {
                    continue;
                }

                if("/policy.wasm".equals(entry.getName())) {
                    bundle.setPolicy(IOUtils.toByteArray(i));
                } else if("/data.json".equals(entry.getName())) {
                    bundle.setData(new String(IOUtils.toByteArray(i)));
                }
            }
        }
        
        return bundle;
    }
}
