package hudson.jumble;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.jumble.report.JumbleRunReport;
import com.jumble.report.ReportWriter;

public class Plugin extends Recorder
{
    private final String reportPath;
    private final Integer targetScore;

    @DataBoundConstructor
    public Plugin(String reportPath, Integer targetScore) 
    {
        this.reportPath = reportPath;
        this.targetScore = targetScore;
    }

    public String getReportPath() {
        return reportPath;
    }

    public Integer getTargetScore() {
        return targetScore;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        InputStream reportStream = null;
        try {
            FilePath report = build.getWorkspace().child(reportPath);
            reportStream = report.read();
            jumbleReport = new ReportWriter().read(reportStream);

            listener.getLogger().println(Messages.plugin_publishingReport());
            copyReportToBuildFolder(build, report);
            build.getActions().add(new ReportBuildAction(jumbleReport, targetScore, build));
            build.getProject().getAction(GraphAction.class).addReportToGraph(jumbleReport, build);

        } catch (FileNotFoundException e) {
            listener.getLogger().println(Messages.plugin_reportNotFound(reportPath));
        } catch (Exception e) {
            listener.getLogger().println("error:" + e.getClass().getCanonicalName() + ":" + e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reportStream);
        }

        return true;
    }

    private void copyReportToBuildFolder(AbstractBuild<?, ?> build, FilePath report) throws IOException,
            InterruptedException {
        FilePath buildTarget = new FilePath(build.getRootDir());
        FilePath toFile = buildTarget.child("jumbleReport.xml");
        report.copyTo(toFile);
    }

    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        return true;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    private JumbleRunReport jumbleReport;

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> 
    {
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName()
        {
            return Messages.plugin_displayName();
        }

        @SuppressWarnings("rawtypes")
        public FormValidation doCheckReportPath(@AncestorInPath AbstractProject project,
                @QueryParameter String value) throws IOException, InterruptedException   {
            if (project.getSomeWorkspace() == null || !project.getSomeWorkspace().child(value).exists()) {
                return FormValidation.error(Messages.plugin_reportPathValidationFailed());
            }
            return FormValidation.ok();
        }

        @SuppressWarnings("rawtypes")
        public FormValidation doCheckTargetScore(@AncestorInPath AbstractProject project,
                @QueryParameter String value) {
            try {
                Integer targetScore = Integer.valueOf(value);
                if (0 <= targetScore && targetScore <= 100) {
                    return FormValidation.ok();
                }
            } catch (NumberFormatException e){}
            return FormValidation.error(Messages.plugin_targetScoreValidationFailed());
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public Collection<Action> getProjectActions(AbstractProject<?, ?> project) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new ProjectScoreAction(project));
        actions.add(new GraphAction(project));
        return Collections.<Action>unmodifiableList(actions);
    }

}

