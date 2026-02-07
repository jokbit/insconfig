package org.jokbit.InsConfig.core;

import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public enum ConfigFileType {
    TOML {
        @Override
        public boolean check(Path path) {
            if (path == null || !Files.exists(path) || !path.getFileName().toString().endsWith(SUFFIX_TOML)) {
                return false;
            }
            try {
                String content = Files.readString(path);
                tomlParser.parse(content);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    },
    PROPERTIES {
        @Override
        public boolean check(Path path) {
            if (path == null || !Files.exists(path) || !path.getFileName().toString().endsWith(SUFFIX_PROPERTIES)) {
                return false;
            }
            try {
                new Properties().load(new FileReader(path.toFile()));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    },
    JSON {
        @Override
        public boolean check(Path path) {
            if (path == null || !Files.exists(path) || !path.getFileName().toString().endsWith(SUFFIX_JSON)) {
                return false;
            }
            try {
                String content = Files.readString(path);
                JsonParser.parseString(content);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    },
    TXT {
        @Override
        public boolean check(Path path) {
            if (!path.getFileName().toString().endsWith(SUFFIX_TXT))
                return false;
            return true;
        }
    },
    YAML {
        @Override
        public boolean check(Path path) {
            if (!path.getFileName().toString().endsWith(SUFFIX_YAML))
                return false;
            return true;
        }
    },
    UNKNOW {

    };

    public boolean check(Path path) {
        return path != null && Files.exists(path);
    }

    public static ConfigFileType parseType(Path path) {
        String fileName = path.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        String ext = lastDotIndex == -1 ? "UNKNOW" : fileName.substring(lastDotIndex + 1);
        try {
            return ConfigFileType.valueOf(ext.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ConfigFileType.UNKNOW;
        }
    }

    public static final String SUFFIX_TOML = ".toml";
    public static final String SUFFIX_PROPERTIES = ".properties";
    public static final String SUFFIX_JSON = ".json";
    public static final String SUFFIX_TXT = ".txt";
    public static final String SUFFIX_YAML = ".yaml";

    private static TomlParser tomlParser = new TomlParser();
}
