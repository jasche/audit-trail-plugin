package hudson.plugins.audit_trail;

import hudson.Util;
import hudson.model.BooleanParameterDefinition;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterDefinition;
import hudson.model.StringParameterDefinition;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import hudson.security.AuthorizationStrategy;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

import hudson.security.SecurityRealm;
import hudson.security.csrf.DefaultCrumbIssuer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.*;

import static org.junit.Assert.*;

public class UserLogListenerTest implements AuditEvents {
    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Issue("JENKINS-???")
    @Test
    public void logOnHappyFlow() throws Exception {
        String logFileName = "logOnHappyFlow.log";
        File logFile = new File(tmpDir.getRoot(), logFileName);
        final String ADMIN = "admin";
        final String ANONYMOUS = "anonymous";

        JenkinsRule.DummySecurityRealm sr = j.createDummySecurityRealm();
        sr.addGroups("administrator", ADMIN);

        AuthorizationStrategy as = new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER).everywhere().to(ADMIN)
                .grant(Jenkins.READ).everywhere().to(ANONYMOUS);


        JenkinsRule.WebClient wc = j.createWebClient();
        new SimpleAuditTrailPluginConfiguratorHelper(logFile).sendConfiguration(j, wc);
        
        j.jenkins.setCrumbIssuer(null);
        j.jenkins.setSecurityRealm(sr);
        j.jenkins.setAuthorizationStrategy(as);
        
        wc.login(ADMIN);
        String log = Util.loadFile(new File(tmpDir.getRoot(), logFileName + ".0"), StandardCharsets.UTF_8);
        Matcher matcher = logInOutRegex.matcher(log);        
        assertTrue("Match",matcher.find());
        assertEquals("Test Event", "login",matcher.group(1));
        assertEquals("Test User", "admin" , matcher.group(2));
        assertNotNull("Test Date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(matcher.group(3)));
    }

    @Issue("JENKINS-???")
    @Test
    public void logOutHappyFlow() throws Exception {
        String logFileName = "logOutHappyFlow.log";
        File logFile = new File(tmpDir.getRoot(), logFileName);
        final String ADMIN = "admin";
        final String ANONYMOUS = "anonymous";

        JenkinsRule.DummySecurityRealm sr = j.createDummySecurityRealm();
        sr.addGroups("administrator", ADMIN);

        AuthorizationStrategy as = new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER).everywhere().to(ADMIN)
                .grant(Jenkins.READ).everywhere().to(ANONYMOUS);
        
        JenkinsRule.WebClient wc = j.createWebClient();
        new SimpleAuditTrailPluginConfiguratorHelper(logFile).sendConfiguration(j, wc);

        j.jenkins.setCrumbIssuer(new DefaultCrumbIssuer(true));
        j.jenkins.setSecurityRealm(sr);
        j.jenkins.setAuthorizationStrategy(as);

        wc.login(ADMIN).goTo("logout");

        String log = Util.loadFile(new File(tmpDir.getRoot(), logFileName + ".0"), StandardCharsets.UTF_8);
        Matcher matcher = logInOutRegex.matcher(log);        
        assertTrue("Match",matcher.find());
        assertTrue("Match",matcher.find());
        assertEquals("Test Event", "logout",matcher.group(1));
        assertEquals("Test User", "admin" , matcher.group(2));
        assertNotNull("Test Date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(matcher.group(3)));
    }

    private static void configureSecurity(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER).everywhere().toEveryone());
        
        j.jenkins.save();
    }
}
