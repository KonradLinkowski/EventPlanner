package dev.eventhub.event.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.eventhub.event.TestcontainersConfiguration;
import dev.eventhub.event.domain.exception.InsufficientSeatsException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** Proves that concurrent seat reservations cannot oversell an event's capacity. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestcontainersConfiguration.class)
class SeatReservationConcurrencyIT {

    @Autowired
    private EventService eventService;

    @Autowired
    private SeatReservationService seatReservationService;

    @Test
    void concurrentReservationsNeverOversellCapacity() throws Exception {
        int capacity = 5;
        int attempts = 20;
        var event = eventService.createEvent(new CreateEventCommand(
                "organizer-1",
                "Sold Out Show",
                "desc",
                "MUSIC",
                "Warsaw",
                "Arena",
                OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now().plusDays(10).plusHours(3),
                new BigDecimal("50.00"),
                "PLN",
                capacity));
        eventService.publishEvent(event.getId());
        UUID eventId = event.getId();

        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch readyLatch = new CountDownLatch(attempts);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger insufficientSeatsCount = new AtomicInteger();

        List<Callable<Void>> tasks = new java.util.ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            tasks.add(() -> {
                readyLatch.countDown();
                startLatch.await();
                try {
                    seatReservationService.reserveSeats(eventId, UUID.randomUUID(), 1);
                    successCount.incrementAndGet();
                } catch (InsufficientSeatsException ex) {
                    insufficientSeatsCount.incrementAndGet();
                }
                return null;
            });
        }

        List<Future<Void>> futures = new java.util.ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }
        readyLatch.await();
        startLatch.countDown();
        for (Future<Void> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(capacity);
        assertThat(insufficientSeatsCount.get()).isEqualTo(attempts - capacity);
        assertThat(eventService.getEvent(eventId).getAvailableSeats()).isEqualTo(0);
    }
}
