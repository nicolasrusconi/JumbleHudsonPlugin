package com.jumble.report;

import java.util.ArrayList;
import java.util.List;


public class MutatedClassReport {

    private final List<Fail> fails = new ArrayList<Fail>();
    private final String name;
    private final String time;
    private Integer point = 0;
    private String score = "0%";
    private Integer passCount = 0;

    private String fullClassName;

    private String errorMessage;

    public MutatedClassReport(String fullClassName,long timeoutInMillis, int mutationCount) {
        this.fullClassName = fullClassName;
        name = parseClassName(fullClassName);

        if (mutationCount > 0 ) {
            point = mutationCount;
        }

        double timeInSeconds = timeoutInMillis / 1000d;
        time=  timeInSeconds + "s";
        updateScore();
    }

    private String parseClassName(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public String getClassName() {
        return fullClassName;
    }

    public void add(Boolean passed, String description) {
        if (passed) {
            passCount++;
        }else {
            fails.add(new Fail(description));
        }
        updateScore();
    }

    public void addError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void updateScore() {
        this.score= calculateScore() + "%";
    }

    private Integer calculateScore() {
        Integer score = 0;
        if (point == 0) {
            score = 100;
        } else
        {
            score = passCount * 100 / point;
        }

        return score;
    }

    public Integer getScore() {
        return calculateScore();
    }

    public String getFormattedScore() {
        return score;
    }

    public List<Fail> getFails() {
        return fails;
    }

    public Integer getPoint() {
        return point;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }
}
