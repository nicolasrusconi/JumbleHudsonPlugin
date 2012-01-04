package com.jumble.report;

public class Fail {

    private static final String COLON = ":";
    private String line;
    private String mutant;

    public Fail(String failDescription) {
        int firstColon = failDescription.indexOf(COLON);
        int secondColon = failDescription.indexOf(COLON, firstColon + 1);

        line = failDescription.substring(firstColon + 1, secondColon);
        mutant = failDescription.substring(secondColon + 2);
    }

    public String getLine() {
        return line;
    }

    public String getMutant() {
        return mutant;
    }
}
