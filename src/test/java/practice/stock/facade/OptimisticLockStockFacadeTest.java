package practice.stock.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import practice.stock.domain.Stock;
import practice.stock.repository.StockRepository;
import practice.stock.service.OptimisticLockStockService;
import practice.stock.service.PessimisticLockStockService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OptimisticLockStockFacadeTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    //테스트 전 데이터 생성
    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    //테스트가 종료되면 데이터 모두 삭제
    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @DisplayName("동시에 100개의 요청(낙관적 락 활용)")
    @Test
    void useOptimisticLock() throws InterruptedException {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }
}