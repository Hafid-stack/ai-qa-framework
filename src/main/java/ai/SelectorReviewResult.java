package ai;

import java.util.List;

public class SelectorReviewResult {

    private final boolean succeeded;
    private final boolean keepElement;
    private final List<String> suggestedMethods;
    private final String concern;
    private final String confidence;
    private final String failureReason;

    public SelectorReviewResult(boolean succeeded, boolean keepElement, List<String> suggestedMethods,
                                String concern, String confidence, String failureReason) {
        this.succeeded = succeeded;
        this.keepElement = keepElement;
        this.suggestedMethods = suggestedMethods;
        this.concern = concern;
        this.confidence = confidence;
        this.failureReason = failureReason;
    }

    public static SelectorReviewResult failed(String reason) {
        return new SelectorReviewResult(false, true, List.of(), "", "unknown", reason);
    }

    public boolean succeeded() { return succeeded; }
    public boolean shouldKeepElement() { return keepElement; }
    public List<String> getSuggestedMethods() { return suggestedMethods; }
    public String getConcern() { return concern; }
    public String getConfidence() { return confidence; }
    public String getFailureReason() { return failureReason; }
}