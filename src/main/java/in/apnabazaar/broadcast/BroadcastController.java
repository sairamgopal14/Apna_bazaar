package in.apnabazaar.broadcast;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @GetMapping("/admin/broadcasts/gaps")
    public List<NudgeGapView> getNudgeGaps(@RequestParam String communitySlug) {
        return broadcastService.getNudgeGaps(communitySlug);
    }

    @PostMapping("/admin/broadcasts/gaps/{zeroResultLogId}/suggestions")
    public NudgeSuggestionsResponse suggestNudges(@RequestParam String communitySlug,
                                                   @PathVariable UUID zeroResultLogId) {
        return broadcastService.generateNudgeSuggestions(communitySlug, zeroResultLogId);
    }

    @PostMapping("/admin/broadcasts")
    public BroadcastResponse createBroadcast(@Valid @RequestBody CreateBroadcastRequest request) {
        return broadcastService.createBroadcast(request);
    }

    @PatchMapping("/admin/broadcasts/{broadcastId}/performance")
    public BroadcastResponse logPerformance(@PathVariable UUID broadcastId,
                                             @Valid @RequestBody UpdatePerformanceRequest request) {
        return broadcastService.logPerformance(broadcastId, request);
    }
}
