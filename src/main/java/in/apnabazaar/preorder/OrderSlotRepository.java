package in.apnabazaar.preorder;

import in.apnabazaar.provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderSlotRepository extends JpaRepository<OrderSlot, UUID> {

    @Query("""
            select os from OrderSlot os
            where os.sessionId = :sessionId and os.rating is null and os.dailyLineItem.itemDate < :today
            """)
    List<OrderSlot> findPendingRatings(UUID sessionId, LocalDate today);

    @Query("select avg(os.rating) from OrderSlot os where os.provider = :provider and os.rating is not null")
    Double averageRatingForProvider(Provider provider);
}
