package openconsignment.di;

import java.util.Map;
import javafx.util.Callback;
import javax.inject.Inject;
import javax.inject.Provider;

public class DaggerControllerFactory implements Callback<Class<?>, Object> {

    private final Map<Class<?>, Provider<Object>> controllerProviders;

    @Inject
    public DaggerControllerFactory(Map<Class<?>, Provider<Object>> controllerProviders) {
        this.controllerProviders = controllerProviders;
    }

    @Override
    public Object call(Class<?> type) {
        Provider<Object> provider = controllerProviders.get(type);
        if (provider != null) {
            return provider.get();
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Unable to construct controller: " + type, ex);
        }
    }
}
