package openconsignment.common;

/** Catalog of repository SQL resources, with paths relative to the classpath's /sql directory. */
public enum SqlQuery {
    BILL_SAVE("bill/save.sql"),
    BILL_SAVE_LR("bill/save-lr.sql"),
    BILL_GET("bill/get.sql"),
    BILL_GET_LRS("bill/get-lrs.sql"),
    BILL_DELETE("bill/delete.sql"),
    BILL_DELETE_LRS("bill/delete-lrs.sql"),
    BILL_GET_ALL("bill/get-all.sql"),

    BUYER_SAVE("buyer/save.sql"),
    BUYER_DELETE("buyer/delete.sql"),
    BUYER_RENAME("buyer/rename.sql"),
    BUYER_GET_ALL("buyer/get-all.sql"),

    COLLECTION_PENDING_BILLS_FOR_BUYER("collection/pending-bills-for-buyer.sql"),
    COLLECTION_PENDING_AMOUNT_FOR_BILL("collection/pending-amount-for-bill.sql"),
    COLLECTION_SAVE("collection/save.sql"),
    COLLECTION_SAVE_ITEM("collection/save-item.sql"),
    COLLECTION_LAST_VOUCHER("collection/last-voucher.sql"),
    COLLECTION_GET_ITEMS("collection/get-items.sql"),
    COLLECTION_GET("collection/get.sql"),
    COLLECTION_DELETE("collection/delete.sql"),
    COLLECTION_DELETE_ITEMS("collection/delete-items.sql"),
    COLLECTION_GET_ALL("collection/get-all.sql"),
    COLLECTION_DETAILS_FOR_BUYER("collection/details-for-buyer.sql"),

    PAYMENT_SAVE("payment/save.sql"),
    PAYMENT_SAVE_ITEM("payment/save-item.sql"),
    PAYMENT_PENDING_BILLS_FOR_SUPPLIER("payment/pending-bills-for-supplier.sql"),
    PAYMENT_PENDING_AMOUNT_FOR_BILL("payment/pending-amount-for-bill.sql"),
    PAYMENT_LAST_VOUCHER("payment/last-voucher.sql"),
    PAYMENT_GET_ITEMS("payment/get-items.sql"),
    PAYMENT_GET("payment/get.sql"),
    PAYMENT_DELETE("payment/delete.sql"),
    PAYMENT_DELETE_ITEMS("payment/delete-items.sql"),
    PAYMENT_GET_ALL("payment/get-all.sql"),
    PAYMENT_DETAILS_FOR_SUPPLIER("payment/details-for-supplier.sql"),

    SUPPLIER_SAVE("supplier/save.sql"),
    SUPPLIER_DELETE("supplier/delete.sql"),
    SUPPLIER_RENAME("supplier/rename.sql"),
    SUPPLIER_GET_ALL("supplier/get-all.sql"),

    TRANSPORT_SAVE("transport/save.sql"),
    TRANSPORT_DELETE("transport/delete.sql"),
    TRANSPORT_RENAME("transport/rename.sql"),
    TRANSPORT_GET_ALL("transport/get-all.sql"),
    TRANSPORT_REPORT("transport/report.sql");

    private final String resourcePath;

    SqlQuery(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String resourcePath() {
        return resourcePath;
    }
}
