package hudson.jumble;

import com.jumble.report.Package;

import hudson.model.Action;
import hudson.model.AbstractBuild;

public class PackageReportAction implements Action {

    private final Package aPackage;
    private final AbstractBuild<?, ?> build;

    public PackageReportAction(Package aPackage, AbstractBuild<?, ?> build) {
        this.aPackage = aPackage;
        this.build = build;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return aPackage.getName();
    }

    public String getUrlName() {
        return "";
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public Package getPackage() {
        return aPackage;
    }
}
