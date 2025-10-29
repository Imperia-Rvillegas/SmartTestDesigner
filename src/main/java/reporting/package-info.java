/**
 * Clases relacionadas con el envío de reportes automáticos.
 * <p>
 * El paquete agrupa los componentes encargados de compartir los resultados
 * de las ejecuciones hacia canales externos:
 * </p>
 * <ul>
 *     <li>Correo electrónico (resumen textual de la suite ejecutada).</li>
 *     <li>Xray Cloud (importación de resultados Cucumber en formato JSON).</li>
 * </ul>
 * <p>
 * Toda la configuración sensible necesaria para establecer las conexiones se
 * obtiene desde un archivo de propiedades ubicado en este mismo paquete y que
 * no debe versionarse. El nombre esperado es
 * {@code reporting-settings.properties}; puede crearse una copia a partir del
 * archivo de ejemplo {@code reporting-settings.example.properties}.
 * </p>
 */
package reporting;
