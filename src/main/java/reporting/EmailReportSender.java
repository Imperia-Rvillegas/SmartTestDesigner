package reporting;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import ui.utils.LogUtil;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Envía por correo electrónico un resumen textual de la ejecución de pruebas.
 *
 * <p>La clase se apoya en la configuración proporcionada por {@link ReportSettings}
 * y reutiliza el formato de los scripts shell previamente usados en el proyecto.
 * El envío puede activarse o desactivarse mediante la propiedad de sistema
 * {@value #SEND_EMAIL_PROPERTY}.</p>
 *
 * <p><strong>Registro (logging):</strong> esta clase utiliza {@code LogUtil}
 * para registrar información y errores durante el proceso de envío.</p>
 *
 * <h2>Propiedades de configuración (claves relevantes)</h2>
 * <ul>
 *   <li><code>sendEmailReport</code> (propiedad de sistema): habilita el envío cuando es {@code true}.</li>
 *   <li><code>email.from</code>: dirección del remitente.</li>
 *   <li><code>email.to</code>: lista de destinatarios (obligatoria, al menos uno).</li>
 *   <li><code>email.cc</code>, <code>email.bcc</code>: listas opcionales de CC y BCC.</li>
 *   <li><code>smtp.host</code>, <code>smtp.port</code> (por defecto 587).</li>
 *   <li><code>smtp.auth</code> (por defecto {@code true}), <code>smtp.starttls</code> (por defecto {@code true}).</li>
 *   <li><code>smtp.connectionTimeout</code>, <code>smtp.timeout</code> (milisegundos, por defecto 10000).</li>
 *   <li><code>smtp.ssl.protocols</code> (opcional, por ejemplo <code>TLSv1.2</code>).</li>
 *   <li><code>smtp.username</code> (opcional; si se omite se usa <code>email.from</code>).</li>
 *   <li><code>smtp.password</code> (obligatoria si <code>smtp.auth=true</code>).</li>
 * </ul>
 *
 * <p><strong>Hilos y reuso:</strong> la clase es inmutable y segura para uso concurrente
 * si se reutiliza la misma instancia desde múltiples hilos. Cada envío crea su propio
 * {@link Message} y utiliza una {@link Session} obtenida para la operación.</p>
 */
public final class EmailReportSender {

    /**
     * Propiedad de sistema que habilita el envío del correo cuando su valor es
     * {@code true}. Puede declararse en la configuración de ejecución (p. ej. en IntelliJ)
     * como {@code -DsendEmailReport=true}.
     */
    public static final String SEND_EMAIL_PROPERTY = "sendEmailReport";

    private final ReportSettings settings;

    /**
     * Crea un nuevo remitente asociado a la configuración indicada.
     *
     * @param settings configuración que aporta credenciales SMTP, remitente y destinatarios.
     * @throws NullPointerException si {@code settings} es {@code null}.
     */
    public EmailReportSender(ReportSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    /**
     * Envía por correo el resumen de la ejecución utilizando la metadata proporcionada.
     *
     * <p>Si la propiedad de sistema {@value #SEND_EMAIL_PROPERTY} no está habilitada
     * (valor distinto de {@code true}), el metodo registra un mensaje informativo y
     * retorna sin realizar envío.</p>
     *
     * <p>El mensaje se envía en texto plano: el asunto y el cuerpo se obtienen de
     * {@link ExecutionReportMetadata#buildEmailSubject()} y
     * {@link ExecutionReportMetadata#buildEmailBody()} respectivamente.</p>
     *
     * @param metadata información de la ejecución (suite, métricas, tiempos, etc.).
     * @throws NullPointerException si {@code metadata} es {@code null}.
     * @throws IllegalStateException si no hay destinatarios en {@code email.to}, si falta
     *                               configuración obligatoria, o si ocurre un error de
     *                               mensajería al enviar el correo.
     */
    public void sendExecutionSummary(ExecutionReportMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");

        if (!Boolean.parseBoolean(System.getProperty(SEND_EMAIL_PROPERTY, "false"))) {
            LogUtil.info(String.format(
                    "Propiedad %s deshabilitada. No se enviará el correo de resumen.",
                    SEND_EMAIL_PROPERTY));
            return;
        }

        List<String> toRecipients = settings.getList("email.to");
        if (toRecipients.isEmpty()) {
            throw new IllegalStateException("Debe configurarse al menos un destinatario en email.to");
        }

        Properties mailProperties = buildMailProperties();
        Session session = createSession(mailProperties);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(settings.getRequired("email.from")));
            message.setRecipients(Message.RecipientType.TO, parseAddresses(toRecipients));
            applyRecipients(message, Message.RecipientType.CC, settings.getList("email.cc"));
            applyRecipients(message, Message.RecipientType.BCC, settings.getList("email.bcc"));
            message.setSubject(metadata.buildEmailSubject());
            message.setText(metadata.buildEmailBody());

            Transport.send(message);
            LogUtil.info(String.format(
                    "Correo con resultados de la suite '%s' enviado exitosamente.",
                    metadata.getSuite()));
        } catch (MessagingException e) {
            LogUtil.error("Error al enviar el correo con el reporte de la suite.", e);
            throw new IllegalStateException("Error al enviar el correo con el reporte de la suite", e);
        }
    }

    /**
     * Construye las propiedades de JavaMail a partir de la configuración.
     *
     * <p>Reconoce las siguientes claves en {@link ReportSettings}:</p>
     * <ul>
     *   <li><code>smtp.host</code> (obligatoria)</li>
     *   <li><code>smtp.port</code> (opcional, por defecto 587)</li>
     *   <li><code>smtp.auth</code> (opcional, por defecto {@code true})</li>
     *   <li><code>smtp.starttls</code> (opcional, por defecto {@code true})</li>
     *   <li><code>smtp.connectionTimeout</code> (opcional, ms; por defecto 10000)</li>
     *   <li><code>smtp.timeout</code> (opcional, ms; por defecto 10000)</li>
     *   <li><code>smtp.ssl.protocols</code> (opcional, p. ej. <code>TLSv1.2</code>)</li>
     * </ul>
     *
     * @return las propiedades listas para crear una {@link Session}.
     * @throws IllegalStateException si falta alguna propiedad obligatoria en {@link ReportSettings}.
     */
    private Properties buildMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", settings.getRequired("smtp.host"));
        properties.put("mail.smtp.port", String.valueOf(settings.getInt("smtp.port", 587)));

        boolean authEnabled = settings.getBoolean("smtp.auth", true);
        boolean startTlsEnabled = settings.getBoolean("smtp.starttls", true);

        properties.put("mail.smtp.auth", String.valueOf(authEnabled));
        properties.put("mail.smtp.starttls.enable", String.valueOf(startTlsEnabled));
        properties.put("mail.smtp.connectiontimeout", String.valueOf(settings.getInt("smtp.connectionTimeout", 10000)));
        properties.put("mail.smtp.timeout", String.valueOf(settings.getInt("smtp.timeout", 10000)));
        settings.get("smtp.ssl.protocols").ifPresent(value -> properties.put("mail.smtp.ssl.protocols", value));
        return properties;
    }

    /**
     * Crea una {@link Session} de JavaMail según las propiedades indicadas.
     *
     * <p>Si <code>mail.smtp.auth</code> es {@code true}, se configura un
     * {@link jakarta.mail.Authenticator} que utiliza:
     * <ul>
     *   <li><code>smtp.username</code> si está presente; en caso contrario, el valor de <code>email.from</code>.</li>
     *   <li><code>smtp.password</code> como contraseña (obligatoria cuando hay autenticación).</li>
     * </ul>
     * Si la autenticación está deshabilitada, retorna una sesión sin autenticador.</p>
     *
     * @param mailProperties propiedades devueltas por {@link #buildMailProperties()}.
     * @return una instancia de {@link Session} lista para enviar mensajes.
     * @throws IllegalStateException si falta <code>smtp.password</code> cuando la autenticación está habilitada,
     *                               o si falta <code>email.from</code> cuando se requiere como usuario.
     */
    private Session createSession(Properties mailProperties) {
        boolean authEnabled = Boolean.parseBoolean(mailProperties.getProperty("mail.smtp.auth", "false"));
        if (!authEnabled) {
            return Session.getInstance(mailProperties);
        }

        final String username = settings.get("smtp.username")
                .orElseGet(() -> settings.getRequired("email.from"));
        final String password = settings.getRequired("smtp.password");

        return Session.getInstance(mailProperties, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Convierte una lista de direcciones de correo en un arreglo de {@link Address}.
     *
     * <p>Cada elemento debe ser una dirección válida RFC 822 aceptada por
     * {@link InternetAddress}.</p>
     *
     * @param addresses lista de direcciones en formato texto.
     * @return un arreglo de {@link Address} para usar en {@link Message#setRecipients(Message.RecipientType, Address[])}.
     * @throws MessagingException si alguna dirección no es válida.
     */
    private Address[] parseAddresses(List<String> addresses) throws MessagingException {
        Address[] result = new Address[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            result[i] = new InternetAddress(addresses.get(i));
        }
        return result;
    }

    /**
     * Aplica destinatarios adicionales (CC o BCC) a un {@link Message} si la lista no está vacía.
     *
     * @param message    el mensaje al que se aplicarán los destinatarios.
     * @param type       el tipo de destinatario ({@link Message.RecipientType#CC} o {@link Message.RecipientType#BCC}).
     * @param recipients lista de direcciones de correo. Si está vacía, no se realiza ninguna acción.
     * @throws MessagingException si alguna dirección no es válida o si falla la operación sobre el {@link Message}.
     */
    private void applyRecipients(Message message, Message.RecipientType type, List<String> recipients)
            throws MessagingException {
        if (recipients.isEmpty()) {
            return;
        }
        message.setRecipients(type, parseAddresses(recipients));
    }
}
