package openconsignment.di;

import dagger.Component;
import javafx.util.Callback;
import javax.inject.Singleton;
import openconsignment.common.AppConfig;

@Singleton
@Component(modules = {DatabaseModule.class, ControllerModule.class, ConfigurationModule.class})
public interface AppComponent {

    AppConfig configuration();

    Callback<Class<?>, Object> controllerFactory();
}
