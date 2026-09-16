package in.apnabazaar.search;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SearchRequest(@NotBlank String communitySlug, @NotBlank String query, UUID sessionId) {
}
