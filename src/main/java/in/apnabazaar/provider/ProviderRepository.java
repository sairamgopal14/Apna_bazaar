package in.apnabazaar.provider;

import in.apnabazaar.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {

    List<Provider> findByCommunityAndStatus(Community community, ProviderStatus status);

    Optional<Provider> findByCommunityAndWhatsappNumber(Community community, String whatsappNumber);
}
