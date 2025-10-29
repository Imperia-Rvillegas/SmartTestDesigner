package ui.enums;

/**
 * Representa los diferentes tipos de accesos posibles dentro de la interfaz de usuario.
 *
 * <p>Los tipos de acceso permiten identificar cómo un usuario navega hacia una sección del sistema:
 * <ul>
 *   <li>{@link #QUICK} - Acceso mediante el menú rápido.</li>
 *   <li>{@link #INDEX} - Acceso mediante el menú índice o principal.</li>
 *   <li>{@link #SEARCH} - Acceso mediante el buscador (ícono de lupa).</li>
 * </ul>
 * </p>
 */
public enum AccessType {

    /**
     * Acceso mediante el menú rápido.
     */
    QUICK,

    /**
     * Acceso mediante el menú índice o principal.
     */
    INDEX,

    /**
     * Acceso mediante el buscador (ícono de lupa).
     */
    SEARCH
}