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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.*;

import static org.junit.Assert.*;

public class SaveableChangeListenerTest implements AuditEvents {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Issue("JENKINS-???")
    @Test
    public void configChangedByAdmin() throws Exception {
        String logFileName = "savableOnHappyFlow.log";
        File logFile = new File(tmpDir.getRoot(), logFileName);
        final String ADMIN = "admin";
        final String ANONYMOUS = "anonymous";

        JenkinsRule.DummySecurityRealm sr = j.createDummySecurityRealm();
        sr.addGroups("administrator", ADMIN);

        AuthorizationStrategy as = new MockAuthorizationStrategy()
                .grant(Jenkins.ADMINISTER).everywhere().to(ADMIN)
                .grant(Jenkins.READ).everywhere().to(ANONYMOUS);


        JenkinsRule.WebClient wc = j.createWebClient();
        
        j.jenkins.setCrumbIssuer(null);
        j.jenkins.setSecurityRealm(sr);
        j.jenkins.setAuthorizationStrategy(as);
        
        wc.login(ADMIN);
        new SimpleAuditTrailPluginConfiguratorHelper(logFile).sendConfiguration(j, wc);
                
        String log = Util.loadFile(new File(tmpDir.getRoot(), logFileName + ".0"), StandardCharsets.UTF_8);
        //assertTrue("logged actions: " + log, log.indexOf("cxonfig » admin changed") > 0);
        Matcher matcher = configChangedRegex.matcher(log);        
        System.out.println("DBG "+ log);
        assertTrue("Match",matcher.find());
        assertEquals("Test file", "audit-trail.xml",matcher.group(1));
        assertEquals("Test user", "admin",matcher.group(2));
        assertNotNull("Test date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(matcher.group(3)));

    }
}
