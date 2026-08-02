package openconsignment.di;

import dagger.Component;
import javafx.util.Callback;
import javax.inject.Singleton;

@Singleton
@Component(modules = {DatabaseModule.class, ControllerModule.class})
public interface AppComponent {

    Callback<Class<?>, Object> controllerFactory();
}
