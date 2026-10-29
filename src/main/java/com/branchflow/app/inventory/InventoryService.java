package com.branchflow.app.inventory;

import com.branchflow.app.branch.Branch;
import com.branchflow.app.branch.BranchRepository;
import com.branchflow.app.common.ConflictException;
import com.branchflow.app.common.NotFoundException;
import com.branchflow.app.product.Product;
import com.branchflow.app.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {
    private final StockItemRepository stockItems;
    private final StockMovementRepository movements;
    private final BranchRepository branches;
    private final ProductRepository products;

    public InventoryService(StockItemRepository stockItems, StockMovementRepository movements,
                            BranchRepository branches, ProductRepository products) {
        this.stockItems = stockItems;
        this.movements = movements;
        this.branches = branches;
        this.products = products;
    }

    @Transactional
    public StockResponse createStockItem(Long branchId, CreateStockItemRequest request) {
        Branch branch = branches.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        Product product = products.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));
        if (!branch.getCompany().getId().equals(product.getCompany().getId())) {
            throw new ConflictException("Product and branch must belong to the same company");
        }
        if (!product.isActive()) throw new ConflictException("Inactive products cannot be added to stock");
        if (stockItems.existsByBranch_IdAndProduct_Id(branchId, product.getId())) {
            throw new ConflictException("This product already has a stock record at the branch");
        }
        return StockResponse.from(stockItems.save(new StockItem(branch, product, request.minimumQuantity())));
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findStock(Long branchId, boolean lowOnly) {
        requireBranch(branchId);
        List<StockItem> items = lowOnly ? stockItems.findLowStockByBranch(branchId)
                : stockItems.findAllByBranch_IdOrderByProduct_NameAsc(branchId);
        return items.stream().map(StockResponse::from).toList();
    }

    @Transactional
    public MovementResponse recordMovement(Long stockItemId, CreateMovementRequest request) {
        StockItem stock = stockItems.findLockedById(stockItemId)
                .orElseThrow(() -> new NotFoundException("Stock item not found: " + stockItemId));
        validateMovement(request.type(), request.quantityChange());
        applyMovement(stock, request.quantityChange());
        return MovementResponse.from(movements.save(new StockMovement(stock, request.type(), request.quantityChange(),
                stock.getQuantity(), request.reason().trim(), Instant.now(), null)));
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> findMovements(Long stockItemId) {
        if (!stockItems.existsById(stockItemId)) throw new NotFoundException("Stock item not found: " + stockItemId);
        return movements.findAllByStockItem_IdOrderByRecordedAtDescIdDesc(stockItemId)
                .stream().map(MovementResponse::from).toList();
    }

    @Transactional
    public List<MovementResponse> transfer(TransferStockRequest request) {
        if (request.fromBranchId().equals(request.toBranchId())) {
            throw new ConflictException("Source and destination branches must be different");
        }
        Branch source = branches.findById(request.fromBranchId())
                .orElseThrow(() -> new NotFoundException("Source branch not found: " + request.fromBranchId()));
        Branch destination = branches.findById(request.toBranchId())
                .orElseThrow(() -> new NotFoundException("Destination branch not found: " + request.toBranchId()));
        Product product = products.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));
        if (!source.getCompany().getId().equals(destination.getCompany().getId())
                || !source.getCompany().getId().equals(product.getCompany().getId())) {
            throw new ConflictException("A transfer must stay within one company and product catalog");
        }

        StockItem sourceItem = stockItems.findByBranch_IdAndProduct_Id(source.getId(), product.getId())
                .orElseThrow(() -> new NotFoundException("Source branch has no stock record for this product"));
        StockItem destinationItem = stockItems.findByBranch_IdAndProduct_Id(destination.getId(), product.getId())
                .orElseThrow(() -> new NotFoundException("Destination branch has no stock record for this product"));
        List<Long> ids = List.of(sourceItem.getId(), destinationItem.getId()).stream().sorted().toList();
        List<StockItem> locked = stockItems.findAllLockedByIds(ids);
        if (locked.size() != 2) throw new ConflictException("Stock records changed during transfer");
        StockItem lockedSource = locked.stream().filter(item -> item.getId().equals(sourceItem.getId())).findFirst().orElseThrow();
        StockItem lockedDestination = locked.stream().filter(item -> item.getId().equals(destinationItem.getId())).findFirst().orElseThrow();

        UUID transferId = UUID.randomUUID();
        int outbound = Math.negateExact(request.quantity());
        applyMovement(lockedSource, outbound);
        applyMovement(lockedDestination, request.quantity());
        Instant now = Instant.now();
        StockMovement out = movements.save(new StockMovement(lockedSource, MovementType.TRANSFER_OUT, outbound,
                lockedSource.getQuantity(), request.reason().trim(), now, transferId));
        StockMovement in = movements.save(new StockMovement(lockedDestination, MovementType.TRANSFER_IN, request.quantity(),
                lockedDestination.getQuantity(), request.reason().trim(), now, transferId));
        return List.of(MovementResponse.from(out), MovementResponse.from(in));
    }

    private void requireBranch(Long branchId) {
        if (!branches.existsById(branchId)) throw new NotFoundException("Branch not found: " + branchId);
    }

    private void validateMovement(MovementType type, int change) {
        if (change == 0) throw new ConflictException("Stock movements must change quantity");
        if ((type == MovementType.DELIVERY || type == MovementType.TRANSFER_IN) && change < 0)
            throw new ConflictException(type + " movements must increase stock");
        if ((type == MovementType.SALE || type == MovementType.DAMAGE || type == MovementType.TRANSFER_OUT) && change > 0)
            throw new ConflictException(type + " movements must decrease stock");
    }

    private void applyMovement(StockItem stock, int change) {
        long updated = (long) stock.getQuantity() + change;
        if (updated < 0) throw new ConflictException("The movement would make stock negative");
        if (updated > Integer.MAX_VALUE) throw new ConflictException("The resulting stock exceeds the supported limit");
        stock.apply(change);
    }
}
