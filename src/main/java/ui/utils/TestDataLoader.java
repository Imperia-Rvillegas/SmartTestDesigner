package ui.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public class TestDataLoader {

    public static <T> T load(String path, Class<T> tipo) {
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(path)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(is, tipo);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando datos de prueba: " + path, e);
        }
    }
}
