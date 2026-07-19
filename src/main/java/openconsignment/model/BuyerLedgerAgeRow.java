package openconsignment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class BuyerLedgerAgeRow {
    private String billNo;
    private String billDate;
    private String billAmount;
    private String supplierName;
    private Integer days;
}
