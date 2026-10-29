package openconsignment.di;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import javafx.util.Callback;
import openconsignment.controller.MainController;
import openconsignment.controller.WelcomeScreenController;
import openconsignment.controller.entry.BillEntryController;
import openconsignment.controller.entry.CollectionEntryController;
import openconsignment.controller.entry.EntryController;
import openconsignment.controller.entry.PaymentEntryController;
import openconsignment.controller.history.BillHistoryController;
import openconsignment.controller.history.CollectionHistoryController;
import openconsignment.controller.history.PaymentHistoryController;
import openconsignment.controller.master.BuyerController;
import openconsignment.controller.master.MasterController;
import openconsignment.controller.master.SupplierController;
import openconsignment.controller.master.TransportController;
import openconsignment.controller.report.BuyerLedgerController;
import openconsignment.controller.report.ReportController;
import openconsignment.controller.report.SupplierLedgerController;
import openconsignment.controller.report.TransportLedgerController;

@Module
@SuppressWarnings("unused")
public interface ControllerModule {

    @Binds
    Callback<Class<?>, Object> bindControllerFactory(DaggerControllerFactory controllerFactory);

    @Binds
    @IntoMap
    @ClassKey(MainController.class)
    Object bindMainController(MainController controller);

    @Binds
    @IntoMap
    @ClassKey(WelcomeScreenController.class)
    Object bindWelcomeScreenController(WelcomeScreenController controller);

    @Binds
    @IntoMap
    @ClassKey(EntryController.class)
    Object bindEntryController(EntryController controller);

    @Binds
    @IntoMap
    @ClassKey(SupplierController.class)
    Object bindSupplierController(SupplierController controller);

    @Binds
    @IntoMap
    @ClassKey(MasterController.class)
    Object bindMasterController(MasterController controller);

    @Binds
    @IntoMap
    @ClassKey(TransportController.class)
    Object bindTransportController(TransportController controller);

    @Binds
    @IntoMap
    @ClassKey(BuyerController.class)
    Object bindBuyerController(BuyerController controller);

    @Binds
    @IntoMap
    @ClassKey(CollectionHistoryController.class)
    Object bindCollectionHistoryController(CollectionHistoryController controller);

    @Binds
    @IntoMap
    @ClassKey(PaymentHistoryController.class)
    Object bindPaymentHistoryController(PaymentHistoryController controller);

    @Binds
    @IntoMap
    @ClassKey(BillHistoryController.class)
    Object bindBillHistoryController(BillHistoryController controller);

    @Binds
    @IntoMap
    @ClassKey(SupplierLedgerController.class)
    Object bindSupplierLedgerController(SupplierLedgerController controller);

    @Binds
    @IntoMap
    @ClassKey(TransportLedgerController.class)
    Object bindTransportLedgerController(TransportLedgerController controller);

    @Binds
    @IntoMap
    @ClassKey(BuyerLedgerController.class)
    Object bindBuyerLedgerController(BuyerLedgerController controller);

    @Binds
    @IntoMap
    @ClassKey(ReportController.class)
    Object bindReportController(ReportController controller);

    @Binds
    @IntoMap
    @ClassKey(CollectionEntryController.class)
    Object bindCollectionEntryController(CollectionEntryController controller);

    @Binds
    @IntoMap
    @ClassKey(BillEntryController.class)
    Object bindBillEntryController(BillEntryController controller);

    @Binds
    @IntoMap
    @ClassKey(PaymentEntryController.class)
    Object bindPaymentEntryController(PaymentEntryController controller);
}
