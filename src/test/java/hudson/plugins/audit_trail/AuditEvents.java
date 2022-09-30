package hudson.plugins.audit_trail;

import java.util.regex.Pattern;

public interface AuditEvents {
    Pattern logInOutRegex = Pattern.compile("(login|logout) » (.*) at (.*)");
    Pattern configChangedRegex = Pattern.compile("config » (.*) changed by (.*) at (.*)");
    Pattern jobStartRegex = Pattern.compile("job\\/(.*)\\/ #[0-9]+ Started by user ([^,]*)[,] Parameters[:][\\[].*[\\]]");
    Pattern jobStopRegex = Pattern.compile("[AP]M (.*) #[0-9]+ Started by user ([^,]*)[,] Parameters[:][\\[].*[\\]] on node (.*) started at (.*)");

}
