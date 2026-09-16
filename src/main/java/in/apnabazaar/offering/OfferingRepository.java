package in.apnabazaar.offering;

import in.apnabazaar.provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {

    List<Offering> findByProvider(Provider provider);

    Optional<Offering> findByProviderAndName(Provider provider, String name);
}
