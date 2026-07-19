package openconsignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class SupplierLedgerRow {

    private String billNo;
    private String billDate;
    private Integer billAmount;

    private String buyerName;

    private String voucherNo;

    private Integer amountPaid;
    private Integer dueAmount;

    private String bank;
    private String ddNo;
    private String ddDate;
}
