package in.apnabazaar.ingestion;

import in.apnabazaar.provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyPostRepository extends JpaRepository<DailyPost, UUID> {

    Optional<DailyPost> findByProviderAndPostDate(Provider provider, LocalDate postDate);
}
