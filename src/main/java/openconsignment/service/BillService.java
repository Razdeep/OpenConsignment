package openconsignment.service;

import java.sql.SQLException;
import java.util.List;
import openconsignment.entity.BillEntity;
import openconsignment.repository.BillRepository;

public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public void saveBill(BillEntity billEntity) throws SQLException {
        billRepository.save(billEntity);
    }

    public BillEntity getBill(String billNo) throws SQLException {
        return billRepository.getBill(billNo);
    }

    public void deleteBill(String billNo) throws SQLException {
        billRepository.deleteBill(billNo);
    }

    public List<BillEntity> getBills() throws SQLException {
        return billRepository.getBills();
    }
}
