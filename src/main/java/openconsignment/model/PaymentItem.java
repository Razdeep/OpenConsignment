package openconsignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaymentItem {
    private String billNo;
    private String billAmount;
    private String billDate;
    private String buyerName;
    private String due;
    private String amountPaid;
    private String bank;
    private String ddNo;
    private String ddDate;
}
