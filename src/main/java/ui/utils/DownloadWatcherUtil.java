package ui.utils;

import java.io.File;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para vigilar un directorio de descargas y detectar la aparición de
 * un archivo nuevo tras una acción de usuario (p. ej., clic en “Descargar”).
 * <p>
 * La instancia marca un <em>punto en el tiempo</em> al construirse y, a partir de
 * ese marcador, escanea periódicamente la carpeta buscando ficheros cuyo nombre
 * cumpla un patrón dado y que no estén en estado de descarga incompleta.
 * Al encontrar un fichero “estable” (tamaño sin cambios durante un breve intervalo),
 * lo renombra añadiendo un sello temporal (timestamp) y devuelve la referencia.
 * </p>
 *
 * <h3>Características</h3>
 * <ul>
 *   <li>Ignora archivos temporales de descarga: extensiones {@code .crdownload} y {@code .part}.</li>
 *   <li>Valida “estabilidad” comprobando que el tamaño no cambia en ~400 ms.</li>
 *   <li>Polling con intervalo aproximado de 250 ms hasta agotar el timeout.</li>
 *   <li>Renombrado con timestamp usando formato {@code yyyyMMdd_HHmmss}.</li>
 * </ul>
 *
 * <h3>Uso típico</h3>
 * <pre>{@code
 * Path downloads = Paths.get("target/downloads/escenario_20250101_101010/");
 * DownloadWatcherUtil watcher = new DownloadWatcherUtil(downloads);
 * File file = watcher.waitNewAndStamp(".*\\.(xlsx|xls|csv|zip)", Duration.ofSeconds(30));
 * }</pre>
 *
 * <p><strong>Nota:</strong> esta clase está pensada para ser usada justo antes de
 * iniciar la descarga (por ejemplo, antes de hacer clic) o inmediatamente después,
 * para que el marcador temporal discrimine correctamente archivos previos.</p>
 */
public class DownloadWatcherUtil {
    /** Directorio a vigilar. */
    private final Path dir;

    /**
     * Marca temporal de referencia: solo se considerarán archivos cuya fecha
     * de modificación sea posterior a este instante.
     */
    private final Instant marker;

    /** Formato del sello temporal usado al renombrar: {@code yyyyMMdd_HHmmss}. */
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Crea un watcher asociado a un directorio y fija el instante actual como
     * marcador para futuras detecciones.
     *
     * @param dir ruta del directorio a monitorizar (debe existir y ser accesible).
     */
    public DownloadWatcherUtil(Path dir) {
        this.dir = dir;
        this.marker = Instant.now();
    }

    /**
     * Espera hasta que aparezca un archivo nuevo en {@link #dir} que:
     * <ul>
     *   <li>Su nombre cumpla la expresión regular dada ({@code regex}).</li>
     *   <li>No termine en {@code .crdownload} ni {@code .part}.</li>
     *   <li>Haya sido modificado después del {@link #marker}.</li>
     *   <li>Sea “estable” (su tamaño no cambia en ~400 ms).</li>
     * </ul>
     * Cuando lo encuentra, lo renombra añadiendo un timestamp al nombre base y devuelve el fichero resultante.
     *
     * <p>El metodo realiza sondeos (polling) cada ~250 ms hasta alcanzar el {@code timeout}.</p>
     *
     * @param regex   expresión regular para filtrar por nombre de archivo (p. ej. {@code ".*\\.(xlsx|csv)"}).
     * @param timeout tiempo máximo a esperar antes de abortar.
     * @return el {@link File} ya renombrado con el sello temporal.
     *
     * @throws RuntimeException si no se detecta una descarga válida dentro del tiempo
     *                          ({@code "No se detectó nueva descarga en <dir>"}), o si
     *                          falla el renombrado ({@code "No se pudo renombrar la descarga a <nombre>"}).
     *
     * @implNote El timestamp se inserta como sufijo del nombre base:
     * <pre>{@code
     * original: reporte.csv
     * final:    reporte_20250101_101010.csv
     * }</pre>
     * Si el nombre no tiene extensión, el sello se agrega al final.
     */
    public File waitNewAndStamp(String regex, Duration timeout) {
        Instant end = Instant.now().plus(timeout);
        File found = null;
        while (Instant.now().isBefore(end)) {
            File[] matches = dir.toFile().listFiles((d, name) ->
                    name.matches(regex) && !name.endsWith(".crdownload") && !name.endsWith(".part"));
            if (matches != null) {
                for (File f : matches) {
                    if (Instant.ofEpochMilli(f.lastModified()).isAfter(marker) && isStable(f.toPath())) {
                        found = f; break;
                    }
                }
            }
            if (found != null) break;
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }
        if (found == null) throw new RuntimeException("No se detectó nueva descarga en " + dir);

        String name = found.getName();
        int dot = name.lastIndexOf('.');
        String base = (dot > 0) ? name.substring(0, dot) : name;
        String ext  = (dot > 0) ? name.substring(dot) : "";
        String stamped = base + "_" + LocalDateTime.now().format(TS) + ext;

        Path target = dir.resolve(stamped);
        try { return Files.move(found.toPath(), target, StandardCopyOption.REPLACE_EXISTING).toFile(); } catch (Exception e) { throw new RuntimeException("No se pudo renombrar la descarga a " + stamped, e); }
    }

    /**
     * Determina si un archivo es “estable” verificando que su tamaño en bytes no cambia
     * en un intervalo corto (~400 ms). Útil para evitar procesar archivos que aún
     * se están descargando o escribiendo.
     *
     * @param p ruta del archivo a comprobar.
     * @return {@code true} si el tamaño permanece constante; {@code false} en caso contrario
     *         o si ocurre alguna excepción al leer el tamaño.
     */
    private boolean isStable(Path p) {
        try {
            long s1 = Files.size(p);
            Thread.sleep(400);
            long s2 = Files.size(p);
            return s1 == s2;
        } catch (Exception e) { return false; }
    }
}
