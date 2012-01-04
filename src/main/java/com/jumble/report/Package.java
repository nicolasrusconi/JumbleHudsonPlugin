package com.jumble.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Package {

    private final String name;
    private String score = "0%"; 
    private List<MutatedClassReport> classes = new ArrayList<MutatedClassReport>();

    public Package(String name) {
        this.name = name;
    }

    public void add(MutatedClassReport report) {
        classes.add(report);
    }

    public Integer processScores() {
        Integer totalScores = Integer.valueOf(0);
        for (MutatedClassReport clazz : classes) {
            totalScores = totalScores + clazz.getScore();
        }
        int packageScore = totalScores / classes.size();
        score = packageScore + "%";
        return packageScore; 
    }

    public String getName() {
        return name;
    }

    public String getScore() {
        return score;
    }

    public List<MutatedClassReport> getReports() {
        return Collections.unmodifiableList(classes);
    }

}
