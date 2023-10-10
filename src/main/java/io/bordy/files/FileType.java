package io.bordy.files;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FileType {
    @JsonProperty("file")
    FILE,
    @JsonProperty("folder")
    FOLDER,
    @JsonProperty("external_file")
    EXTERNAL_FILE
}
