package hudson.jumble;


import hudson.model.ProminentProjectAction;
import hudson.model.AbstractProject;
import hudson.model.Actionable;

public class ProjectScoreAction implements ProminentProjectAction {

    /**
     * Project that owns this action.
     */
    public final AbstractProject<?,?> project;

    public ProjectScoreAction(AbstractProject<?,?> project) {
        this.project = project;
    }

    public String getIconFileName() {
        return "clipboard.gif";
    }

    public String getDisplayName() {
        if (lasCompletedBuildHasJumbleReport()) {
            return Messages.projectScore_displayName();
        } else {
            return Messages.projectScore_noReportsYet();
        }
    }

    public String getUrlName() {
        if (lasCompletedBuildHasJumbleReport()) {
            return "lastCompletedBuild/jumblereport";
        }
        else {
            return "";
        }
    }

    private boolean lasCompletedBuildHasJumbleReport() {
        Actionable build = (Actionable) project.getLastCompletedBuild();
        return build !=null && build.getAction(ReportBuildAction.class) != null;
    }

}
