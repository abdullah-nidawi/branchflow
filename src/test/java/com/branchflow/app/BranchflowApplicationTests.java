package com.branchflow.app;

import com.branchflow.app.branch.Branch;
import com.branchflow.app.branch.BranchRepository;
import com.branchflow.app.branch.Company;
import com.branchflow.app.common.ConflictException;
import com.branchflow.app.inventory.*;
import com.branchflow.app.product.Product;
import com.branchflow.app.product.ProductRepository;
import com.branchflow.app.scheduling.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchflowApplicationTests {
    @Mock private StockItemRepository stockItems;
    @Mock private StockMovementRepository movements;
    @Mock private BranchRepository branches;
    @Mock private ProductRepository products;
    @Mock private EmployeeRepository employees;
    @Mock private ShiftRepository shifts;

    @Test
    void deliveryRecordsLedgerAndUpdatesOnHandQuantity() {
        Company company = new Company("Northstar Retail");
        StockItem stock = new StockItem(new Branch(company, "Center", "Dortmund"),
                new Product(company, "COLA-1L", "Cola 1 L"), 4);
        when(stockItems.findLockedById(7L)).thenReturn(Optional.of(stock));
        when(movements.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryService inventory = new InventoryService(stockItems, movements, branches, products);
        MovementResponse result = inventory.recordMovement(7L,
                new CreateMovementRequest(MovementType.DELIVERY, 12, "Supplier delivery"));

        assertEquals(12, stock.getQuantity());
        assertEquals(12, result.quantityAfter());
        assertEquals(12, result.quantityChange());
        assertEquals(MovementType.DELIVERY, result.type());
        verify(movements).save(any(StockMovement.class));
    }

    @Test
    void movementThatWouldMakeStockNegativeIsRejectedWithoutLedgerWrite() {
        Company company = new Company("Northstar Retail");
        StockItem stock = new StockItem(new Branch(company, "Center", "Dortmund"),
                new Product(company, "COLA-1L", "Cola 1 L"), 0);
        when(stockItems.findLockedById(7L)).thenReturn(Optional.of(stock));
        InventoryService inventory = new InventoryService(stockItems, movements, branches, products);

        assertThrows(ConflictException.class, () -> inventory.recordMovement(7L,
                new CreateMovementRequest(MovementType.SALE, -1, "Sale")));
        assertEquals(0, stock.getQuantity());
        verify(movements, never()).save(any(StockMovement.class));
    }

    @Test
    void overlappingEmployeeShiftIsRejected() {
        Branch branch = new Branch(new Company("Northstar Retail"), "Center", "Dortmund");
        ReflectionTestUtils.setField(branch, "id", 3L);
        Employee employee = new Employee(branch, "Alex Example", "alex@example.test");
        ReflectionTestUtils.setField(employee, "id", 9L);
        when(branches.findById(3L)).thenReturn(Optional.of(branch));
        when(employees.findLockedById(9L)).thenReturn(Optional.of(employee));
        when(shifts.hasOverlap(eq(9L), any(Instant.class), any(Instant.class), eq(ShiftStatus.CANCELLED), isNull()))
                .thenReturn(true);
        SchedulingService scheduling = new SchedulingService(employees, shifts, branches);
        Instant start = Instant.parse("2026-10-30T08:00:00Z");
        Instant end = Instant.parse("2026-10-30T16:00:00Z");

        assertThrows(ConflictException.class, () -> scheduling.createShift(3L,
                new CreateShiftRequest(9L, start, end)));
        verify(shifts, never()).save(any(Shift.class));
    }
}
