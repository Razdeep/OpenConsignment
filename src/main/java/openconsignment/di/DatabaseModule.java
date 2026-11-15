package openconsignment.di;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
@SuppressWarnings("unused")
public class DatabaseModule {

    @Provides
    @Singleton
    static DatabaseManager provideDatabaseManager() {
        return new DatabaseManager(null);
    }
}
