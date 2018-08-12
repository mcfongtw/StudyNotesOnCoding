package com.github.mcfongtw.io;

import com.codahale.metrics.ScheduledReporter;
import com.github.mcfongtw.utils.SudoExecutors;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractIoBenchmark {

    protected static final int NUM_ITERATION = 100;

    private static final int UNIT_ONE_KILO = 1024;

    private static final int UNIT_ONE_MEGA = UNIT_ONE_KILO * UNIT_ONE_KILO;

    protected static final int TOTAL_DATA_WRITEN = 1 * UNIT_ONE_MEGA;

    protected static ScheduledReporter metricReporter = InfluxdbReporterSingleton.newInstance();

    protected static abstract class AbstractExecutionPlan implements ExecutableLifecycle {
        protected Logger logger = LoggerFactory.getLogger(this.getClass());

        private String sudoPassword = "";


        public AbstractExecutionPlan() {
            logger.info("Starting reporting metric...");
            metricReporter.start(500, TimeUnit.MILLISECONDS);
            logger.info("Starting reporting metric...DONE");

            String sudoPasswordVal = System.getProperty("sudoPassword");
            if(StringUtils.isNotEmpty(sudoPasswordVal)) {
                sudoPassword = sudoPasswordVal;
            }
            logger.debug("SudoPassword: {}", sudoPassword);
        }

        @Override
        public void preTrialSetUp() throws Exception {
            logger.trace("[preTrialSetUp]");
        }

        @Override
        public void doTrialSetUp() throws Exception {
            this.preTrialSetUp();
            logger.trace("[doTrialSetUp]");
            this.postTrialSetUp();
        }

        @Override
        public void postTrialSetUp() throws IOException, InterruptedException {
            logger.trace("[postTrialSetUp]");

            if(StringUtils.isNotEmpty(sudoPassword)) {
                logger.info("Start to dropping free pagecache, dentries and inodes...");
                SudoExecutors.exec("echo 3 > /proc/sys/vm/drop_caches", sudoPassword);
                logger.info("Start to dropping free pagecache, dentries and inodes...DONE");
            }
        }

        @Override
        public void preTrialTearDown() throws Exception {
            logger.trace("[preTrialTearDown]");

        }

        @Override
        public void doTrialTearDown() throws Exception {
            this.preTrialTearDown();
            logger.trace("[doTrialTearDown]");
            this.postTrialTearDown();
        }

        @Override
        public void postTrialTearDown() throws Exception {
            logger.trace("[postTrialTearDown]");
            logger.info("Stopping reporting metric...");
            metricReporter.stop();
            logger.info("Stopping reporting metric...DONE");
        }

        @Override
        public void preIterationSetup() throws Exception {
            logger.trace("[preIterationSetup]");
        }

        @Override
        public void doIterationSetup() throws Exception {
            this.preIterationSetup();
            logger.trace("[doIterationSetup]");
            this.postIterationSetup();
        }

        @Override
        public void postIterationSetup()  throws Exception {
            logger.trace("[postIterationSetup]");
        }

        @Override
        public void preIterationTearDown() throws Exception{
            logger.trace("[preIterationTearDown]");
        }

        @Override
        public void doIterationTearDown() throws Exception{
            this.preIterationTearDown();
            logger.trace("[doIterationTearDown]");
            this.postIterationTearDown();
        }

        @Override
        public void postIterationTearDown()  throws Exception{
            logger.trace("[postIterationTearDown]");
        }

    }


    protected static abstract class AbstractSequentialExecutionPlan extends AbstractExecutionPlan {

        protected String finPath;

        protected String foutPath;

        protected File tempDir;

        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();

            tempDir = Files.createTempDir();
            new File(tempDir.getAbsolutePath()).mkdirs();

            finPath = tempDir.getAbsolutePath() + "/in.data";
            foutPath = tempDir.getAbsolutePath() + "/out.data";

            FileUtils.touch(new File(finPath));
            FileUtils.touch(new File(foutPath));

            try(
                    FileOutputStream fin = new FileOutputStream(finPath);
            ) {
                for(int i = 0; i < TOTAL_DATA_WRITEN; i++) {
                    fin.write((byte) i);
                }
            }

            logger.debug("Temp dir created at [{}]", tempDir.getAbsolutePath());
            logger.debug("File created at [{}]", finPath);
            logger.debug("File created at [{}]", foutPath);
        }

        @Override
        public void postTrialTearDown() throws Exception {
            super.postTrialTearDown();

            FileUtils.deleteDirectory(tempDir);
            logger.debug("Temp dir deleted at [{}]", tempDir.getAbsolutePath());
        }
    }

}
