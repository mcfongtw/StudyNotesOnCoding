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

    protected static abstract class AbstractExecutionPlan {
        protected Logger logger = LoggerFactory.getLogger(this.getClass());

        private volatile  boolean enableReporter = false;

        private String sudoPassword = "";

        protected String finPath;

        protected String foutPath;

        protected File tempDir;

        public AbstractExecutionPlan() {
            String propVal = System.getProperty("enableReporter");
            if(StringUtils.isNotEmpty(propVal)) {
                enableReporter = Boolean.parseBoolean(propVal);
            }

            logger.info("Metric Reporter Status: [{}]", enableReporter);

            if(enableReporter) {
                //TODO: Read reporting interval from configuration
                metricReporter.start(500, TimeUnit.MILLISECONDS);

                logger.info("Starting reporting metric");
            }

            String sudoPasswordVal = System.getProperty("sudoPassword");
            if(StringUtils.isNotEmpty(sudoPasswordVal)) {
                sudoPassword = sudoPasswordVal;
            }
            logger.debug("SudoPassword: {}", sudoPassword);
        }


        @Override
        protected void finalize() {
            if(enableReporter) {
                metricReporter.stop();
            }
        }

        public void setUp() throws IOException, InterruptedException {
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

            if(StringUtils.isNotEmpty(sudoPassword)) {
                logger.info("Start to dropping free pagecache, dentries and inodes...");
                SudoExecutors.exec("echo 3 > /proc/sys/vm/drop_caches", sudoPassword);
                logger.info("Start to dropping free pagecache, dentries and inodes...DONE");
            }
        }

        public void tearDown() throws IOException, InterruptedException {
            FileUtils.deleteDirectory(tempDir);


        }


    }



}
