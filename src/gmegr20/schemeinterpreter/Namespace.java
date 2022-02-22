package gmegr20.schemeinterpreter;

import java.util.ArrayList;
import java.util.HashMap;

class Namespace {
    private final HashMap<String, NamespaceItem> identifiers = new HashMap<>();

    public void addLiteral(String name, String value) {
        identifiers.put(name, new NamespaceItem(value));
    }

    public void addLambda(String name, ArrayList<String> parameters, String value) {
        identifiers.put(name, new NamespaceItem(parameters, value));
    }

    public Namespace() {

    }

    public Namespace (Namespace from) {
        for(String s : from.identifiers.keySet()) {
            identifiers.put(s, from.identifiers.get(s));
        }
    }

    public boolean contains(String identifier) {
        return identifiers.containsKey(identifier);
    }

    public NamespaceItem get(String identifier) {
        return identifiers.get(identifier);
    }
}
