package hooks;

import io.cucumber.java.AfterAll;
import ui.utils.LogUtil;
import reporting.EmailReportSender;
import reporting.ExecutionReportMetadata;
import reporting.ReportSettings;
import reporting.XrayReportUploader;

/**
 * Hook de ciclo de vida que coordina el envío de reportes externos una vez que
 * finaliza la ejecución de Cucumber.
 *
 * <p>El envío del correo y la publicación en Xray se controlan mediante las
 * propiedades de sistema {@link EmailReportSender#SEND_EMAIL_PROPERTY} y
 * {@link XrayReportUploader#SEND_XRAY_PROPERTY}. Si ambas están deshabilitadas,
 * el hook termina sin realizar acciones.</p>
 *
 * <p><strong>Registro (logging):</strong> este hook utiliza {@code LogUtil} para
 * registrar información y errores durante la publicación de reportes.</p>
 */
public final class ReportingLifecycleHook {

    private ReportingLifecycleHook() {
        // Clase de utilería; no instanciable.
    }

    /**
     * Ejecutado automáticamente por Cucumber una sola vez cuando finaliza la
     * suite. Carga la configuración sensible, recopila metadata de la ejecución
     * y delega en los servicios de {@link EmailReportSender} y
     * {@link XrayReportUploader}.
     */
    @AfterAll
    public static void publishReports() {
        final boolean emailEnabled = Boolean.parseBoolean(
                System.getProperty(EmailReportSender.SEND_EMAIL_PROPERTY, "false"));
        final boolean xrayEnabled = Boolean.parseBoolean(
                System.getProperty(XrayReportUploader.SEND_XRAY_PROPERTY, "false"));

        if (!emailEnabled && !xrayEnabled) {
            LogUtil.info(String.format(
                    "Propiedades %s y %s deshabilitadas. Se omite el envío externo.",
                    EmailReportSender.SEND_EMAIL_PROPERTY, XrayReportUploader.SEND_XRAY_PROPERTY));
            return;
        }

        LogUtil.info(String.format(
                "Programando publicación de reportes para la fase de apagado de la JVM (email=%s, xray=%s).",
                emailEnabled, xrayEnabled));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogUtil.info("Iniciando publicación de reportes tras finalizar la ejecución de Cucumber…");

            ReportSettings settings = ReportSettings.load();
            ExecutionReportMetadata metadata = ExecutionReportMetadata.collect(settings);

            if (xrayEnabled) {
                try {
                    new XrayReportUploader(settings).uploadCucumberResults(metadata);
                } catch (Exception e) {
                    LogUtil.error("No fue posible publicar el reporte de resultados en Xray.", e);
                }
            }

            if (emailEnabled) {
                try {
                    new EmailReportSender(settings).sendExecutionSummary(metadata);
                } catch (Exception e) {
                    LogUtil.error("No fue posible enviar el correo con el resumen de la suite.", e);
                }
            }
        }, "reporting-publisher"));
    }
}
