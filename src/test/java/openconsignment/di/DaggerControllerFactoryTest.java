package openconsignment.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.inject.Provider;
import org.junit.jupiter.api.Test;

class DaggerControllerFactoryTest {

    @Test
    void callReturnsInstanceFromRegisteredProvider() {
        Object controller = new Object();
        @SuppressWarnings("unchecked")
        Provider<Object> provider = mock(Provider.class);
        when(provider.get()).thenReturn(controller);
        DaggerControllerFactory factory = new DaggerControllerFactory(Map.of(Object.class, provider));

        assertEquals(controller, factory.call(Object.class));
    }

    @Test
    void callFallsBackToReflectiveNoArgConstructorForUnregisteredType() {
        DaggerControllerFactory factory = new DaggerControllerFactory(Map.of());

        Object result = factory.call(NoArgCtorFixture.class);

        assertInstanceOf(NoArgCtorFixture.class, result);
    }

    @Test
    void callWrapsReflectiveFailureInRuntimeException() {
        DaggerControllerFactory factory = new DaggerControllerFactory(Map.of());

        assertThrows(RuntimeException.class, () -> factory.call(NoNoArgCtorFixture.class));
    }

    public static class NoArgCtorFixture {}

    public static class NoNoArgCtorFixture {
        public NoNoArgCtorFixture(String required) {}
    }
}
