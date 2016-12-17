package com.library.masterdaemon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.library.datamodel.Constants.APIContentType;
import com.library.httpconnmanager.HttpClientPool;
import com.library.jettyhttpserver.CustomJettyServer;
import com.library.scheduler.CustomJobScheduler;
import com.library.utilities.BindXmlAndPojo;
import com.library.configs.JobsConfig;
import com.library.sgsharedinterface.SharedAppConfigIF;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
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
