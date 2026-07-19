package openconsignment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransportLedgerRow {
    private String lr;
    private String pm;
    private String billNo;
    private String supplierName;
    private String buyerName;
    private String lrDate;
}
