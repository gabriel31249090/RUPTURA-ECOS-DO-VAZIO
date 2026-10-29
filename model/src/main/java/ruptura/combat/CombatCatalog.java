package ruptura.combat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the deliberately flat, data-only JSON catalogs without a framework dependency. */
final class CombatCatalog {
    private static final Pattern OBJECT = Pattern.compile("\\{([^{}]+)}");
    private static final Pattern FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+))");
    private final Map<String, Map<String, String>> entries;

    CombatCatalog(String file) {
        try {
            String json;
            try (InputStream stream = CombatCatalog.class.getResourceAsStream("/data/" + file)) {
                json = stream == null ? Files.readString(Path.of("data", file), StandardCharsets.UTF_8)
                        : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            entries = parse(json);
            if (entries.isEmpty()) throw new IOException("Catálogo vazio");
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível carregar data/" + file, ex);
        }
    }

    private static Map<String, Map<String, String>> parse(String json) {
        Map<String, Map<String, String>> records = new LinkedHashMap<>();
        Matcher objects = OBJECT.matcher(json);
        while (objects.find()) {
            Map<String, String> record = new LinkedHashMap<>();
            Matcher fields = FIELD.matcher(objects.group(1));
            while (fields.find()) record.put(fields.group(1), fields.group(2) == null ? fields.group(3) : fields.group(2));
            if (record.containsKey("id")) records.put(record.get("id"), record);
        }
        return records;
    }

    Map<String, String> get(String id) {
        Map<String, String> result = entries.get(id);
        if (result == null) throw new IllegalArgumentException("Entrada ausente: " + id);
        return result;
    }

    static int number(Map<String, String> value, String field) {
        return Integer.parseInt(value.get(field));
    }
}
