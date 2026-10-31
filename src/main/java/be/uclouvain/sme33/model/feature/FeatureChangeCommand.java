package be.uclouvain.sme33.model.feature;

import java.util.List;

/** Command pattern: one value object represents one atomic transition request. */
public record FeatureChangeCommand(List<String> deactivate, List<String> activate) {
    public FeatureChangeCommand {
        deactivate = List.copyOf(deactivate);
        activate = List.copyOf(activate);
    }
}
