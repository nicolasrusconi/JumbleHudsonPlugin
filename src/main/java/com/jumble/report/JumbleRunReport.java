package com.jumble.report;

import java.util.ArrayList; 
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JumbleRunReport {

    private String score = "0%";
    private Integer totalScore = 0;
    private List<Package> packages = new ArrayList<Package>();
    private Map<String, Package> packagesMap = new HashMap<String, Package>(); 

    public void add(MutatedClassReport report) {
        String packageName = parsePackagName(report.getClassName());
        Package thePackage = packagesMap.get(packageName);

        if (thePackage == null) {
            thePackage = new Package(packageName);
            packagesMap.put(packageName, thePackage);
            packages.add(thePackage);
        }
        thePackage.add(report);
    }

    private String parsePackagName(String className) {
        int lastDotIndex = className.lastIndexOf(".");
        return className.substring(0, lastDotIndex);
    }

    public String processScores() {
        totalScore = 0;
        for (Package aPackage : packages) {
            totalScore = totalScore + aPackage.processScores();
        }
        score = totalScore / packages.size() + "%";
        return score;
    }

    public List<Package> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public String getScore() {
        return score;
    }

    public Integer getScoreAsInt() {
        return Integer.valueOf(score.substring(0, score.length() - 1));
    }
}
