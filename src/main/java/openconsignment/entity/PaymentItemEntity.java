package openconsignment.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentItemEntity {
    private String voucherNo;
    private String buyerName;
    private String billNo;
    private String billDate;
    private String billAmount;
    private String dueAmount;
    private String amountPaid;
    private String bank;
    private String ddNo;
    private String ddDate;
}
