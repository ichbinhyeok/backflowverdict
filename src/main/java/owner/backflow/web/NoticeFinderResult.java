package owner.backflow.web;

import java.util.List;

public record NoticeFinderResult(
        String label,
        String path,
        String resultType,
        String summary,
        List<String> signals,
        int score
) {
    public NoticeFinderResult {
        signals = signals == null ? List.of() : List.copyOf(signals);
    }
}
