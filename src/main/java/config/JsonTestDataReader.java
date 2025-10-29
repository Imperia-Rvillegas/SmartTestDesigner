package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Clase para leer archivos JSON de bodies y reemplazar dinámicamente los valores.
 */
public class JsonTestDataReader {

    /**
     * Lee un archivo JSON y lo devuelve como String.
     * @param filePath Ruta del archivo.
     * @return Contenido del archivo en String.
     * @throws IOException si ocurre un error al leer el archivo.
     */
    private static String readJsonFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    /**
     * Obtiene el body de la petición con los valores reemplazados.
     * @param fileName Nombre del archivo JSON (por ejemplo, "createUser.json").
     * @param replacements Mapa con los valores a reemplazar.
     * @return JSON con los placeholders reemplazados.
     * @throws IOException si ocurre un error al leer el archivo.
     */
    public static String getRequestBody(String fileName, Map<String, String> replacements) throws IOException {
        String jsonBody = readJsonFile("src/test/resources/testdata/bodyRequest/" + fileName);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            jsonBody = jsonBody.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return jsonBody;
    }
}
