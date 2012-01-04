package hudson.jumble;

import java.awt.Color; 
import java.io.IOException;

import javax.servlet.ServletException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.jumble.report.JumbleRunReport;

import hudson.Functions;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.util.Area;
import hudson.util.ColorPalette;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

public class GraphAction implements Action {

    private static final String TARGET_SERIES = "target";
    private static final String SCORE_SERIES = "score";
    private final AbstractProject<?,?> project;
    private DefaultCategoryDataset dataset;

    public GraphAction(AbstractProject<?,?> project) {
        this.project = project;
        initializeDataset();
    }

    private void initializeDataset() {
        AbstractBuild<?,?> build = findFirstBuildWithJumble(project);

        dataset = new DefaultCategoryDataset();
        while(build != null) {
            JumbleRunReport report = lookupReportFor(build);
            if (report != null) {
                addReportToGraph(report, build);
            }
            build = build.getNextBuild();
        }
    }

    private AbstractBuild<?, ?> findFirstBuildWithJumble(AbstractProject<?, ?> project2) {
        AbstractBuild<?,?> build = project.getFirstBuild();
        while(lookupReportFor(build) == null) {
            build = build.getNextBuild();
        }
        return build;
    }

    private JumbleRunReport lookupReportFor(AbstractBuild<?,?> build){
        ReportBuildAction action = lookupJumbleAction(build);
        if (action != null) {
            return action.getJumbleReport();
        }
        return null;
    }

    private ReportBuildAction lookupJumbleAction(AbstractBuild<?, ?> build) {
        ReportBuildAction action = build.getAction(ReportBuildAction.class);
        return action;
    }

    public void addReportToGraph(JumbleRunReport report, AbstractBuild<?, ?> build) {
        dataset.setValue(null, SCORE_SERIES, new NumberOnlyBuildLabel(build));
        dataset.setValue(null, TARGET_SERIES, new NumberOnlyBuildLabel(build));

        Integer score = report.getScoreAsInt();
        dataset.addValue(score, SCORE_SERIES,  new NumberOnlyBuildLabel(build));

        Integer targetScore = lookupTargetScorefor(build);
        Integer diff = targetScore - score;
        if (diff < 0) {
            diff = 0;
        }

        dataset.addValue(diff, TARGET_SERIES,  new NumberOnlyBuildLabel(build));
    }

    private Integer lookupTargetScorefor(AbstractBuild<?,?> build){
        ReportBuildAction action = lookupJumbleAction(build);
        Integer targetScore = Integer.valueOf(0);
        if (action != null && action.getTargetScore() != null) {
            targetScore = action.getTargetScore();
        }
        return targetScore;
    }

    public void doTrend(final StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        Area graphSize = calculateDefaultSize();
        new Graph(project.getLastBuild().getTimestamp(),graphSize.width,graphSize.height) {
            protected JFreeChart createGraph() {
                return createChart(request, dataset);
            }
        }.doPng(request,response);
    }

    public void doTrendMap(final StaplerRequest request, StaplerResponse response) throws IOException {
        Area graphSize = calculateDefaultSize();
        new Graph(project.getLastBuild().getTimestamp(),graphSize.width,graphSize.height) {
            protected JFreeChart createGraph() {
                return createChart(request, dataset);
            }
        }.doMap(request,response);
    }

    private Area calculateDefaultSize() {
        Area res = Functions.getScreenResolution();
        if(res!=null && res.width<=800)
            return new Area(250,100);
        else
            return new Area(500,200);
    }

    private JFreeChart createChart(StaplerRequest req,CategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createStackedAreaChart(null, null, SCORE_SERIES, dataset,
                PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);
        plot.setInsets(new RectangleInsets(0,0,0,5.0));

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        NumberAxis ax =(NumberAxis) plot.getRangeAxis();
        ax.setRange(new Range(0, 105));
        ax.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        @SuppressWarnings("serial")
        StackedAreaRenderer ar = new StackedAreaRenderer2() {
            @Override
            public String generateURL(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                return label.build.getNumber()+ "/" + ReportBuildAction.REPORT_URL + "/";
            }

            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                switch (row) {
                    case 0:
                        return Messages.graph_scoreLabel(label.build.getDisplayName(), lookupReportFor(label.build).getScore());
                    case 1:
                        return Messages.graph_targetLabel(label.build.getDisplayName(), lookupTargetScorefor(label.build));
                    default:
                        return Messages.graph_notApplicable();
                }
            }
        };
        plot.setRenderer(ar);
        plot.getRenderer().setSeriesPaint(0, ColorPalette.BLUE);
        plot.getRenderer().setSeriesPaint(1, ColorPalette.RED);

        return chart;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return Messages.graph_displayName();
    }

    public String getUrlName() {
        return "jumbleGraph";
    }

    public DefaultCategoryDataset getDataset() {
        return dataset;
    }

    public void setDataset(DefaultCategoryDataset dataset) {
        this.dataset = dataset;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }
}
