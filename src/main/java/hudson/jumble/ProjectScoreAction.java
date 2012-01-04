package hudson.jumble;


import hudson.model.ProminentProjectAction;
import hudson.model.AbstractProject;

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
        return Messages.projectScore_displayName();
    }

    public String getUrlName() {
        return "lastCompletedBuild/jumblereport";
    }

}
