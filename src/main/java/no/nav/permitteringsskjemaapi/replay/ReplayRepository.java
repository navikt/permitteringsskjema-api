package no.nav.permitteringsskjemaapi.replay;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

public interface ReplayRepository extends JpaRepository<ReplayQueueItem, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            value = "select * from replay_queue_items limit 10",
            nativeQuery = true
    )
    List<ReplayQueueItem> fetchReplayQueueItems();
}
