package openconsignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class BuyerLedgerRow {

    private String billNo;
    private String billDate;
    private Integer billAmount;

    private String supplierName;

    private String voucherNo;

    private Integer amountCollected;
    private Integer collectionDue;

    private String bank;
    private String ddNo;
    private String ddDate;
}
