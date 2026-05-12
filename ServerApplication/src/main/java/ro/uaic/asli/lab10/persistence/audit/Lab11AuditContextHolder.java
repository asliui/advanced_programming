package ro.uaic.asli.lab10.persistence.audit;

/**
 * Carries the current principal name for Spring Data JPA auditing on worker threads.
 */
public final class Lab11AuditContextHolder {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private Lab11AuditContextHolder() {
    }

    public static void setPrincipal(String name) {
        if (name == null || name.isBlank()) {
            CURRENT.remove();
        } else {
            CURRENT.set(name);
        }
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static String currentPrincipalOrNull() {
        return CURRENT.get();
    }
}
