package openconsignment.entity;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CollectionEntity {
    private String voucherNo;
    private String voucherDate;
    private String buyerName;
    private String totalAmount;
    private List<CollectionItemEntity> items;
}
