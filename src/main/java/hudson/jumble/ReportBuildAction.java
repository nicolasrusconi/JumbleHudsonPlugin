package hudson.jumble;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.jumble.report.JumbleRunReport;
import com.jumble.report.Package;

import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.AbstractBuild;

public class ReportBuildAction implements HealthReportingAction {

    public static final String REPORT_URL = "jumblereport";
    private final JumbleRunReport jumbleReport;
    private final Integer targetScore;
    private final AbstractBuild<?, ?> build;
    private Map<String, Package> packagesByName = new HashMap<String, Package>();

    public ReportBuildAction(JumbleRunReport jumbleReport, Integer targetScore, AbstractBuild<?, ?> build) {
        this.jumbleReport = jumbleReport;
        this.targetScore = targetScore;
        this.build = build;

        for (Package aPackage : jumbleReport.getPackages()) {
            packagesByName.put(aPackage.getName(), aPackage);
        }
    }

    public String getIconFileName() {
        return "clipboard.gif";
    }

    public String getDisplayName() {
        return Messages.reportBuild_displayName();
    }

    public String getUrlName() {
        return REPORT_URL;
    }

    public HealthReport getBuildHealth() {
        Integer score = jumbleReport.getScoreAsInt();
        Integer buildHealth = 100;
        if (targetScore != null) {
            buildHealth = (score * 100) / targetScore;
            if (buildHealth > 100) {
                buildHealth = 100;
            }
        }
        return new HealthReport(buildHealth, Messages._reportBuild_jumbleScore());
    }

    public String getScore() {
        return jumbleReport.getScore();
    }

    public Integer getTargetScore() {
        return targetScore;
    }

    public JumbleRunReport getJumbleReport() {
        return jumbleReport;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public Object getDynamic(String token, StaplerRequest req,
            StaplerResponse rsp) {
        Package aPackage = packagesByName.get(token);
        PackageReportAction action = null;
        if (aPackage != null) {
            action = new PackageReportAction(aPackage, build);
        }
        return action;
    }
}
