package com.library.masterdaemon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.library.datamodel.Constants.NamedConstants;
import com.library.httpconnmanager.HttpClientPool;
import com.library.httpcontrollers.HttpMainController;
import com.library.jettyhttpserver.CustomJettyServer;
import com.library.scheduler.CustomJobScheduler;
import com.library.masterdaemon.utilities.BindXmlAndPojo;
import com.library.scheduler.JobsData;
import com.library.sgsharedinterface.SharedAppConfigIF;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.SAXException;
//import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.quartz.Job;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.slf4j.Logger;

/**
 *
 * @author smallgod
 */
public class MasterDaemon implements Daemon, ServletContextListener {

    private DaemonContext daemonContext;
    private CustomJettyServer jettyServer;
    private CustomJobScheduler jobScheduler;
    private HttpClientPool httpClientPool;
    //private SharedAppConfigIF sharedAppConfigs;

    //Daemon methods
    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        daemonContext = context;
        jobScheduler = new CustomJobScheduler();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void destroy() {
    }

    //ServletContextListener methods
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("MasterDaemon's contextInitialized method called: " + sce.getClass().getName());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("MasterDaemon's contextDestroyed method called: " + sce.getClass().getName());
    }

    //custom extra functionality
//    protected <T> SharedAppConfigIF loadAppProps(String xmlFilePath, String xsdFilePath, Class<T> classToBind) throws FileNotFoundException, UnsupportedEncodingException, SAXException, ValidationException, JAXBException, NullPointerException {
//
//        System.out.println("About to unmarshal and cast to SharedAppConfigIF");
//        this.sharedAppConfigsIF = (SharedAppConfigIF) BindXmlAndPojo.xmlFileToObject(xmlFilePath, xsdFilePath, classToBind);
//        System.out.println("Done unmarshalling and casting to SharedAppConfigIF");
//        return this.sharedAppConfigsIF;
//
//    }
    //custom extra functionality
    protected <T> Object loadAppProps(String xmlFilePath, String xsdFilePath, Class<T> classToBind) throws FileNotFoundException, UnsupportedEncodingException, SAXException, ValidationException, JAXBException, NullPointerException {

        Object appConfigsJaxb = BindXmlAndPojo.xmlFileToObject(xmlFilePath, xsdFilePath, classToBind);
        return appConfigsJaxb;

    }

    /**
     * Load log4j properties
     *
     * @param log4jFile
     * @param paramsToPass - logs dir path is passed at index 0, others can
     * follow
     * @throws java.lang.Exception
     */
    protected void loadLog4JProps(String log4jFile, String... paramsToPass) throws Exception {

        //Properties props = new Properties();
        //props.put("logsFolder", paramsToPass);
        //DOMConfigurator.setParameter(elem, propSetter, props);
        DOMConfigurator.configure(log4jFile); //XML configurator
        MapLookup.setMainArguments(paramsToPass);
        //PropertyConfigurator.configure(log4jPropsFileLoc);//Property file configurator

    }

    protected void loadHibernateProps() {

    }

    protected String[] loadCmdLineArgs() throws NullPointerException {

        String[] commandLineArgs;

        try {
            commandLineArgs = daemonContext.getArguments();
        } catch (Exception ex) {
            throw new NullPointerException("Failed to read the command line args from the daemon context - " + ex.getMessage());
        }

        System.out.println("--------------------------------------------------------------------");
        System.out.println("                  | -- CmdLine arguments loaded: " + commandLineArgs.length + " -- |                ");
        System.out.println("--------------------------------------------------------------------");

        int x = 1;
        for (String cmdLineArg : commandLineArgs) {
            System.out.println("cmdLineArg. " + x + " : " + cmdLineArg);
            x++;
        }

        System.out.println("--------------------------------------------------------------------");

        return commandLineArgs;
    }

    void addHttpControllers() {

    }

    /**
     * Initialise the http connection manager
     *
     * @param sharedConfigs
     * @param apiContentType The kind of requests whether Json or Xml that we
     * will be sending/recieving
     * @return
     */
    protected HttpClientPool initialiseHttpClientPool(SharedAppConfigIF sharedConfigs, String apiContentType) {

        if (httpClientPool == null) {

            int readTimeout = sharedConfigs.getReadTimeout();
            int connTimeout = sharedConfigs.getConnTimeout();
            int connPerRoute = sharedConfigs.getConnPerRoute();
            int maxConnections = sharedConfigs.getMaxConnections();

            httpClientPool = new HttpClientPool(readTimeout, connTimeout, connPerRoute, maxConnections, apiContentType);

        }

        return httpClientPool;
    }

    protected CustomJettyServer attachJettyServer(SharedAppConfigIF sharedAppConfigsIF, Logger LOGGER) throws FileNotFoundException {

        String contextPath = sharedAppConfigsIF.getContextpath();
        String webAppWarFile = sharedAppConfigsIF.getWebappwarfile();
        int HTTP_PORT = sharedAppConfigsIF.getHttpport();
        int HTTPS_PORT = sharedAppConfigsIF.getHttpsport();
        int ADMIN_PORT = sharedAppConfigsIF.getAdminport();
        int OUTPUT_BUFFER_SIZE = sharedAppConfigsIF.getOutputbuffersize();
        int REQUEST_HEADER_SIZE = sharedAppConfigsIF.getRequestheadersize();
        int RESPONSE_HEADER_SIZE = sharedAppConfigsIF.getResponseheadersize();
        String KEYSTORE_PATH = sharedAppConfigsIF.getKeystorepass();
        String KEYSTORE_PASS = sharedAppConfigsIF.getKeystorepass();
        String KEYSTORE_MGR_PASS = sharedAppConfigsIF.getKeystoremanagerpass();
        String[] WELCOME_FILES = (sharedAppConfigsIF.getWelcomefiles().trim()).split("\\s*,\\s*");

        String resourceBase = sharedAppConfigsIF.getResourceDirAbsPath();
        String webDescriptor = resourceBase + sharedAppConfigsIF.getWebxmlfile();
        //our localServer for accepting external requests
        jettyServer = new CustomJettyServer(webDescriptor, resourceBase, contextPath, webAppWarFile, HTTP_PORT, LOGGER);

        //add other httpConfigs
        //HttpConfiguration httpConfig = jettyServer.addHTTPConfigs(OUTPUT_BUFFER_SIZE, REQUEST_HEADER_SIZE, RESPONSE_HEADER_SIZE, Boolean.TRUE, Boolean.TRUE);
        jettyServer.addHTTPConfigs(OUTPUT_BUFFER_SIZE, REQUEST_HEADER_SIZE, RESPONSE_HEADER_SIZE, Boolean.TRUE, Boolean.TRUE);

        //connectors        
        //jettyServer.addHttpsConnector(HTTPS_PORT, KEYSTORE_PATH, KEYSTORE_PASS, KEYSTORE_MGR_PASS);
        //jettyServer.addAdminConnector(ADMIN_PORT);
        //Connector httpConnector = jettyServer.addHttpConnector(httpConfig, HTTP_PORT);
        jettyServer.addHttpConnector(HTTP_PORT);

        //handlers
        //jettyServer.addResourceHandler(WELCOME_FILES, Boolean.TRUE);
        //jettyServer.addContextHandler(WELCOME_FILES);
        //jettyServer.getServletContextHandler(WELCOME_FILES);
        jettyServer.addWebAppContextHandler();

        //jettyServer.addConnector(httpConnector);
        //jettyServer.addHandler(webAppContextHandler);
        return jettyServer;
    }

    protected boolean startServer() throws Exception {
        return (jettyServer.startServer());
    }

    protected boolean stopServer() throws Exception {
        return (jettyServer.stopServer());
    }

    protected void scheduleARepeatJob(SharedAppConfigIF sharedAppConfigs, Class<? extends Job> jobClass, JobListener jobListener, HttpClientPool httpClientPool) {

        JobsData jobsData = new JobsData();

        String apiContentType = httpClientPool.getApiContentType();
        String remoteUrl;

        if (NamedConstants.HTTP_CONTENT_TYPE_JSON.equalsIgnoreCase(apiContentType)) {
            remoteUrl = sharedAppConfigs.getCentralServerJsonUrl();
        } else {
            //Xml
            remoteUrl = sharedAppConfigs.getCentralServerXmlUrl();
        }
        
        jobsData.setRemoteUrl(remoteUrl); //we will need to write proper logic to test if this is a Json or Xml request
        jobsData.setHttpClientPool(httpClientPool);

        String triggerName = sharedAppConfigs.getAdFetcherTriggerName();
        String jobName = sharedAppConfigs.getAdFetcherJobName();
        String groupName = sharedAppConfigs.getAdFetcherGroupName();
        int repeatInterval = sharedAppConfigs.getAdFetcherInterval();

        //Class<? extends Job> jobClass
        jobScheduler.scheduleARepeatJob(triggerName, jobName, groupName, repeatInterval, jobsData, jobClass, jobListener);

    }

    protected boolean pauseAJob(String jobName, String groupName) {
        return (jobScheduler.pauseAJob(jobName, groupName));
    }

    protected boolean resumeAJob(String jobName, String groupName) {
        return (jobScheduler.resumeAJob(jobName, groupName));
    }

    protected void scheduleAOneTimeJob(String triggerName, String jobName) {

    }

    protected void cancelAllJobs() throws SchedulerException {

        jobScheduler.cancelAllJobs();
    }

    /**
     * Method will remove this trigger from the given Job, but other triggers if
     * any associated if not removed will still fire this job use deleteJob() to
     * completely remove the entire job with all its associated triggers
     *
     * @param triggerName of the trigger to be removed from the job
     */
    protected void deleteATrigger(String triggerName) {
        jobScheduler.deleteATrigger(triggerName);

    }

    //re-arrange things - put this method in the right location
    protected void failDaemon(String failureMsg, Exception failureException) {

        System.err.println("FATAL ERROR: Failed to start Daemon: " + failureMsg);

        try {
            daemonContext.getController().fail(failureMsg, failureException);
        } catch (IllegalStateException ise) {
            System.err.println("ERROR: IllegalStateException while failing/shutting down daemon due to previous errors: " + ise.getMessage());
        }

    }

    //re-arrange things - put this method in the right location
    protected void failDaemon(String failureMsg) {

        System.err.println("FATAL ERROR: Failed to start Daemon: " + failureMsg);
        daemonContext.getController().fail(failureMsg);

    }

    //re-arrange things - put this method in the right location
    protected void failDaemon(Exception failureException) {

        System.err.println("FATAL ERROR: Failed to start Daemon: " + failureException.getMessage());
        daemonContext.getController().fail(failureException);

    }
}
