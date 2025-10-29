package config;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase para almacenar datos compartidos entre pruebas.
 */
public class TestContext {
    private static final Map<String, String> dataStore = new HashMap<>();

    /**
     * Almacena un valor con la clave especificada.
     * @param key Clave.
     * @param value Valor.
     */
    public static void setValue(String key, String value) {
        dataStore.put(key, value);
    }

    /**
     * Recupera el valor almacenado para la clave.
     * @param key Clave.
     * @return Valor asociado o null si no existe.
     */
    public static String getValue(String key) {
        return dataStore.get(key);
    }
}
