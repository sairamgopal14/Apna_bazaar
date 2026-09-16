package in.apnabazaar.offering;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A buyer-facing tag for an offering (e.g. "Batter," "Tiffin," "Services"). Unlike
 * {@link OfferingType}, this isn't a fixed enum -- an admin adds a brand-new one just
 * by typing it into the Excel sheet's Category column, with no code change or redeploy.
 */
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    protected Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
