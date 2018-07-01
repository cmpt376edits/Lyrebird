package moe.lyrebird.lyrebirdserver.model;

import org.springframework.stereotype.Component;
import moe.lyrebird.lyrebirdserver.model.objects.LyrebirdVersion;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

@Component
public class VersionService {

    public LyrebirdVersion getLatestVersion() throws MalformedURLException {
        return new LyrebirdVersion(
                "1.0.1",
                "101",
                new URL("https://tristan.moe"),
                Collections.emptyList()
        );
    }

}