package in.apnabazaar.search;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        return searchService.search(request);
    }

    @PostMapping("/search/{offeringId}/click")
    public void recordClick(@PathVariable UUID offeringId) {
        searchService.recordClick(offeringId);
    }

    @GetMapping("/digest")
    public DigestResponse getDigest(@RequestParam String communitySlug) {
        return searchService.getDigest(communitySlug);
    }
}
