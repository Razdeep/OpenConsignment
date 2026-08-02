package openconsignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import openconsignment.entity.BillEntity;
import openconsignment.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillService billService;

    @Test
    void saveBillDelegatesToRepository() throws SQLException {
        BillEntity billEntity = BillEntity.builder().billNo("B1").build();

        billService.saveBill(billEntity);

        verify(billRepository).save(billEntity);
    }

    @Test
    void getBillReturnsRepositoryResult() throws SQLException {
        BillEntity billEntity = BillEntity.builder().billNo("B1").build();
        when(billRepository.getBill("B1")).thenReturn(billEntity);

        BillEntity result = billService.getBill("B1");

        assertSame(billEntity, result);
    }

    @Test
    void deleteBillDelegatesToRepository() throws SQLException {
        billService.deleteBill("B1");

        verify(billRepository).deleteBill("B1");
    }

    @Test
    void getBillsReturnsRepositoryResult() throws SQLException {
        List<BillEntity> bills = List.of(BillEntity.builder().billNo("B1").build());
        when(billRepository.getBills()).thenReturn(bills);

        assertEquals(bills, billService.getBills());
    }

    @Test
    void getBillPropagatesRepositoryException() throws SQLException {
        when(billRepository.getBill("B1")).thenThrow(new SQLException("boom"));

        assertThrows(SQLException.class, () -> billService.getBill("B1"));
    }
}
