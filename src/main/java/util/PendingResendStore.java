package util;

import jakarta.servlet.ServletContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PendingResendStore {

    private PendingResendStore() {
    }

    @SuppressWarnings("unchecked")
    public static Set<Integer> getSet(ServletContext context, String key, boolean create) {
        if (context == null || key == null || key.isBlank()) {
            return null;
        }

        synchronized (context) {
            Object value = context.getAttribute(key);
            if (value instanceof Set<?>) {
                return (Set<Integer>) value;
            }

            if (!create) {
                return null;
            }

            Set<Integer> set = ConcurrentHashMap.newKeySet();
            context.setAttribute(key, set);
            return set;
        }
    }
}
