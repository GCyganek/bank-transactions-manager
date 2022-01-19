package settings.json;

import com.google.gson.*;
import settings.SettingsConfig;
import settings.SettingsParser;
import model.util.SourceType;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonSettingsParser implements SettingsParser {
    private String path;
    private String dateTimeFormat;
    private Gson gson;

    private class SourceTypeDeserializer implements JsonDeserializer<SourceType> {
        @Override
        public SourceType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return SourceType.fromString(json.getAsString()).orElseThrow();
        }
    }

    private class DateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DateTimeFormatter.ofPattern(dateTimeFormat)));
        }
    }

    private class DateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern(dateTimeFormat));
        }
    }

    public JsonSettingsParser(String path, String dateTimeFormat) {
        this.path = path;
        this.dateTimeFormat = dateTimeFormat;
        this.gson = configureGson();
    }

    private Gson configureGson() {
        return new GsonBuilder()
                .setDateFormat(dateTimeFormat)
                .registerTypeAdapter(SourceType.class, new SourceTypeDeserializer())
                .registerTypeAdapter(LocalDateTime.class, new DateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer())
                .create();
    }

    public SettingsConfig loadSettingsConfig() throws IOException {
        try (FileReader reader = new FileReader(this.path);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return this.gson.fromJson(bufferedReader, JsonSettingsConfig.class);
        }
    }


    public void saveSettingsConfig(SettingsConfig config) throws IOException {
        try (FileWriter writer = new FileWriter(this.path);
             BufferedWriter bufferedWriter = new BufferedWriter(writer))  {
            this.gson.toJson(config, JsonSettingsConfig.class, bufferedWriter);
        }
    }
}
