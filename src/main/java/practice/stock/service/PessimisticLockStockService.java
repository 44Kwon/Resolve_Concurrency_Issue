package practice.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.stock.domain.Stock;
import practice.stock.repository.StockRepository;

@Service
public class PessimisticLockStockService {

    private final StockRepository stockRepository;

    public PessimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);

        stock.decrease(quantity);
        stockRepository.save(stock);
    }
}
