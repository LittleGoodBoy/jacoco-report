package org.jacoco.daling.report;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.diff.GitAdapter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;

public class DalingReportGenerator {

    private final String title;
    private final String branchName;
    private final String projectDirectory;
    private final File executionDataFile;
    private final File classesDirectory;
    private final File sourceDirectory;
    private final File reportDirectory;
    private ExecFileLoader execFileLoader;

//	public DalingReportGenerator(final File projectDirectory) {
//		this.title = projectDirectory.getName();
//		this.executionDataFile = new File(projectDirectory, "9100.exec");//第一步生成的exec的文件
//		this.classesDirectory = new File(projectDirectory, "target/classes");//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
//		this.sourceDirectory = new File(projectDirectory, "src/main/java");//源码目录
//		this.reportDirectory = new File(projectDirectory, "coverageReport");////要保存报告的地址
//	}

    public DalingReportGenerator(String projectDirectory, String execName, String branchName) {
        this.branchName = branchName;
        this.projectDirectory = projectDirectory;
        this.title = new File(projectDirectory).getName();
        File projectDirectoryFile = new File(projectDirectory);
        this.executionDataFile = new File(projectDirectoryFile, execName);//第一步生成的exec的文件
        this.classesDirectory = new File(projectDirectoryFile, "target/classes");//目录下必须包含源码编译过的class文件,用来统计覆盖率。所以这里用server打出的jar包地址即可,运行的jar或者Class目录
        this.sourceDirectory = new File(projectDirectoryFile, "src/main/java");//源码目录
        this.reportDirectory = new File(projectDirectoryFile, "coveragereport");////要保存报告的地址
    }

    public void create() throws IOException {
        // Read the jacoco.exec file. Multiple data files could be merged
        // at this point
        loadExecutionData();

        // Run the structure analyzer on a single class folder to build up
        // the coverage model. The process would be similar if your classes
        // were in a jar file. Typically you would create a bundle for each
        // class folder and each jar you want in your report. If you have
        // more than one bundle you will need to add a grouping node to your
        // report
        final IBundleCoverage bundleCoverage = analyzeStructure();

        createReport(bundleCoverage);
    }

    private void createReport(final IBundleCoverage bundleCoverage)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDirectory));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(), execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));


//		//多源码路径
//        MultiSourceFileLocator sourceLocator = new MultiSourceFileLocator(4);
//        sourceLocator.add( new DirectorySourceFileLocator(sourceDirectory1, "utf-8", 4));
//        sourceLocator.add( new DirectorySourceFileLocator(sourceDirectory2, "utf-8", 4));
//        sourceLocator.add( new DirectorySourceFileLocator(sourceDirectoryN, "utf-8", 4));
//        visitor.visitBundle(bundleCoverage,sourceLocator);

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData() throws IOException {
        execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        //git登录授权
//		GitAdapter.setCredentialsProvider("yanfuchang", "yanfuchang?1993");
        //全量覆盖
//		final CoverageBuilder coverageBuilder = new CoverageBuilder();

        //基于分支比较覆盖，参数1：本地仓库，参数2：开发分支（预发分支），参数3：基线分支(不传时默认为master)
        //本地Git路径，新分支 第三个参数不传时默认比较maser，传参数为待比较的基线分支
//        final CoverageBuilder coverageBuilder = new CoverageBuilder("/Users/yanfuchang/版本库/git_space/Jacoco-test", "yanfuchang");
        final CoverageBuilder coverageBuilder = new CoverageBuilder(projectDirectory, branchName);

        //基于Tag比较的覆盖 参数1：本地仓库，参数2：代码分支，参数3：新Tag(预发版本)，参数4：基线Tag（变更前的版本）
        //final CoverageBuilder coverageBuilder = new CoverageBuilder("E:\\Git-pro\\JacocoTest","daily","v004","v003");

        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

        analyzer.analyzeAll(classesDirectory);

        return coverageBuilder.getBundle(title);
    }

    /**
     * 开始生成覆盖率报告
     *  args[0]: 本地git仓库目录,例如："/Users/yanfuchang/版本库/git_space/Jacoco-test"
     *  args[1]：本地exec文件名称,例如："9100.exec"
     *  args[2]：测试的分支名称,例如："yanfuchang"
     */
    public static void main(final String[] args) throws IOException {
//		final DalingReportGenerator generator = new DalingReportGenerator(new File("/Users/yanfuchang/版本库/git_space/Jacoco-test"));
        final DalingReportGenerator generator = new DalingReportGenerator(args[0], args[1], args[2]);
        generator.create();
    }
}