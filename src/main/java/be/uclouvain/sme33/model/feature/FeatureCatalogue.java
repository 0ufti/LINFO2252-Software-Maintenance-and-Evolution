package be.uclouvain.sme33.model.feature;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Reads feature names from the original FeatureIDE XML, rather than duplicating its vocabulary. */
public final class FeatureCatalogue {
    private final Set<String> declared;

    public FeatureCatalogue() {
        try (InputStream input = FeatureCatalogue.class.getResourceAsStream("/feature_model.xml")) {
            if (input == null) throw new IllegalStateException("Missing feature_model.xml resource");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var document = factory.newDocumentBuilder().parse(input);
            NodeList nodes = document.getElementsByTagName("struct").item(0).getChildNodes();
            Set<String> names = new LinkedHashSet<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i) instanceof Element element) collect(element, names);
            }
            declared = Collections.unmodifiableSet(names);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read FeatureIDE feature model", exception);
        }
    }

    private static void collect(Element node, Set<String> names) {
        if (node.hasAttribute("name")) names.add(node.getAttribute("name"));
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child && !child.getTagName().equals("graphics")) {
                collect(child, names);
            }
        }
    }

    public boolean contains(String name) { return declared.contains(name); }
    public Set<String> names() { return declared; }
}
