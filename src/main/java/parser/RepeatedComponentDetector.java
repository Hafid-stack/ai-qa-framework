package parser;

import org.jsoup.nodes.Element;

import java.util.*;
import java.util.stream.Collectors;

// Detects elements that repeat as siblings under the same parent (e.g. one product card
// repeated per item in a grid) and treats each repetition as one instance of a single
// reusable component, instead of generating separate fields for every duplicate selector.
//
// Grouping is keyed by (parent identity, tag, first class) rather than by class name alone:
// a class name can legitimately appear several times across unrelated sections of a page
// (e.g. Bootstrap's "row"/"container" wrappers) without those elements being a repeated
// list — what makes something a "card" is that several elements share the same parent.
public class RepeatedComponentDetector {

    private static final int MIN_REPEATS_TO_COUNT_AS_PATTERN = 3;
    private final HtmlParser htmlParser = new HtmlParser();

    // Scans `scope` for repeating card structures and removes each matched card from the
    // DOM as it is captured. Because the cards are physically removed, whatever the caller
    // extracts from `scope` afterwards is guaranteed to only contain the page's own,
    // non-repeated elements — no separate identity-matching/filtering step required.
    public List<RepeatedComponentGroup> detectAndStrip(Element scope) {
        Map<String, List<Element>> bySiblingSignature = groupBySiblingSignature(scope);

        List<Map.Entry<String, List<Element>>> qualifying = bySiblingSignature.entrySet().stream()
                .filter(e -> e.getValue().size() >= MIN_REPEATS_TO_COUNT_AS_PATTERN)
                .collect(Collectors.toList());

        // Keep only the outermost boundary of each repeating pattern: if a candidate's
        // elements are all nested inside another candidate's elements (e.g. ".single-products"
        // repeating inside ".col-sm-4", which also repeats), drop the inner one — the outer
        // element is the real card boundary and already contains everything the inner one has.
        List<Map.Entry<String, List<Element>>> outermost = qualifying.stream()
                .filter(entry -> qualifying.stream().noneMatch(other ->
                        other != entry && isNestedIn(entry.getValue(), other.getValue())))
                .collect(Collectors.toList());

        List<RepeatedComponentGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<Element>> entry : outermost) {
            List<List<ExtractedElement>> instances = new ArrayList<>();
            for (Element card : entry.getValue()) {
                List<ExtractedElement> instanceElements = htmlParser.extractStructuredFrom(card);
                if (!instanceElements.isEmpty()) {
                    instances.add(instanceElements);
                }
            }
            if (instances.size() >= MIN_REPEATS_TO_COUNT_AS_PATTERN) {
                // Strip the internal parent-identity prefix back off — callers need a plain
                // "tag.class" they can both log and reuse directly as a CSS selector fragment.
                String cardSelector = entry.getKey().substring(entry.getKey().indexOf('|') + 1);

                // A bare tag+class selector for the card (e.g. "div.col-sm-4") can also match
                // unrelated one-off elements elsewhere on the page that happen to reuse the
                // same class. Since every card in this group was matched because it shares
                // ONE specific parent, capture that parent's own tag+class too, so callers can
                // scope the runtime selector to "parent > card" instead of a page-wide search.
                Element sharedParent = entry.getValue().get(0).parent();
                String parentSelector = tagClassSelector(sharedParent);

                groups.add(new RepeatedComponentGroup(cardSelector, instances, parentSelector));
            }
        }

        // Strip every matched card boundary out of the DOM, even ones whose instances
        // turned out to have no interactive elements (e.g. decorative repeated wrappers) —
        // they must not linger to be picked up again by the caller's flat extraction.
        for (Map.Entry<String, List<Element>> entry : outermost) {
            entry.getValue().forEach(Element::remove);
        }

        return groups;
    }

    private Map<String, List<Element>> groupBySiblingSignature(Element scope) {
        Map<String, List<Element>> bySignature = new LinkedHashMap<>();
        for (Element el : scope.select("[class]")) {
            Element parent = el.parent();
            if (parent == null) continue;

            String firstClass = el.className().trim().split("\\s+")[0];
            if (firstClass.isEmpty()) continue;

            String signature = System.identityHashCode(parent) + "|" + el.tagName() + "." + firstClass;
            bySignature.computeIfAbsent(signature, k -> new ArrayList<>()).add(el);
        }
        return bySignature;
    }

    private String tagClassSelector(Element el) {
        if (el == null) return "";
        String firstClass = el.className().trim().split("\\s+")[0];
        return el.tagName() + (firstClass.isEmpty() ? "" : "." + firstClass);
    }

    // True if every element of `candidate` sits inside some element of `possibleParent`.
    private boolean isNestedIn(List<Element> candidate, List<Element> possibleParent) {
        return candidate.stream().allMatch(el ->
                possibleParent.stream().anyMatch(parent -> parent != el && isDescendant(el, parent)));
    }

    private boolean isDescendant(Element el, Element possibleAncestor) {
        Element current = el.parent();
        while (current != null) {
            if (current == possibleAncestor) return true;
            current = current.parent();
        }
        return false;
    }
}
