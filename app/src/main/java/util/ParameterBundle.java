package util;

import java.util.ArrayList;

public class ParameterBundle {

    private ArrayList<Parameter> parameters;

    public ParameterBundle() {
        parameters = new ArrayList<>();
    }

    public ArrayList<Parameter> getParameters() {
        return (ArrayList<Parameter>) parameters.clone();
    }

    public boolean containsParameter(String name) {
        return getParameter(name) != null;
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public void addParameter(String parameter, String value) {
        parameters.add(new Parameter(parameter, value));
    }

    public String getParameter(String name) {
        for (Parameter p : parameters) {
            if (p.name.equals(name)) {
                return p.value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (parameters.size() >= 1) {
            sb.append(parameters.get(0));
        }
        for (int i = 1; i < parameters.size(); i++) {
            sb.append('&');
            sb.append(parameters.get(i));
        }
        return sb.toString();
    }
}
