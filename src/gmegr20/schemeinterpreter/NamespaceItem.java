package gmegr20.schemeinterpreter;

import java.util.ArrayList;

class NamespaceItem {
    private final String value;
    private final ArrayList<String> parameters;
    private boolean isLambdaExpression = false;

    public NamespaceItem(String value) {
        this.value = value;
        this.parameters = null;
    }

    public NamespaceItem(ArrayList<String> parameters, String value) {
        this.value = value;
        this.parameters = parameters;
        isLambdaExpression = true;
    }

    public int getParameterCount() {
        if(parameters == null)
            return 0;
        else
            return parameters.size();
    }

    public String getParameter(int i) {
        assert parameters != null;
        return parameters.get(i);
    }

    public boolean isFunction() {
        return isLambdaExpression;
    }

    public String getValue() {
        return value;
    }
}