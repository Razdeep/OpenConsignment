package openconsignment.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class BillEntity {
    private String supplierName;
    private String buyerName;
    private String billNo;
    private String billDate;
    private String transport;
    private String lrDate;
    private String billAmount;
    private List<LREntity> lrEntities;
}
