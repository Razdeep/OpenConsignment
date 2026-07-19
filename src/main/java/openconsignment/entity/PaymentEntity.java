package openconsignment.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class PaymentEntity {
    private String voucherNo;
    private String voucherDate;
    private String supplierName;
    private String totalAmount;
    private List<PaymentItemEntity> items;
}
