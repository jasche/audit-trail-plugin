package hudson.plugins.audit_trail;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.User;
import jenkins.security.SecurityListener;

import javax.inject.Inject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

//import org.springframework.security.core.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetails;

/**
 * Listener notified of user login and logout events.
 */
@Extension
public class UserLogListener extends SecurityListener {

    @Inject
    AuditTrailPlugin configuration;

    /**
     * Fired when a user has logged in, event logged via Log4j-audit.
     *
     * @param username name or ID of the user who logged in.
     */
    @Override
    protected void loggedIn(@NonNull String username) {
        StringBuilder builder = new StringBuilder(100);
        User u = User.get(username);
        builder.append("login » " + u.getDisplayName() + " at " + currentDateTimeISO());
       
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString(); 
            logger.log(message);
        }
    }

    @Override
    protected void  authenticated(@NonNull UserDetails details) {
        StringBuilder builder = new StringBuilder(100);
       
        builder.append("authenticated » " + details.getUsername() + " at " + currentDateTimeISO());
       
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString(); 
            logger.log(message);
        }
    }

    @Override
    protected void  userCreated(@NonNull String username) {

        StringBuilder builder = new StringBuilder(100);
       
        builder.append("user created » " + username + " at " + currentDateTimeISO());
       
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString(); 
            logger.log(message);
        }
    }

    /**
     * Fired when a user has logged out, event logged via Log4j-audit.
     *
     * @param username name or ID of the user who logged out.
     */
    @Override
    protected void loggedOut(@NonNull String username) {
        StringBuilder builder = new StringBuilder(100);
        User u = User.get(username);
        builder.append("logout » " + u.getDisplayName() + " at " + currentDateTimeISO());
       
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString();
            logger.log(message);
        }
    }

    @Override
    protected void failedToAuthenticate(@NonNull String username) {
 
        StringBuilder builder = new StringBuilder(100);

        builder.append("Failed to authenticate » " + username + " at " + currentDateTimeISO());
       
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString(); 
            logger.log(message);
        }
    }

    @Override
    protected void 	failedToLogIn(@NonNull String username) {
        StringBuilder builder = new StringBuilder(100);
        
        builder.append("Failed to login » " + username + " at " + currentDateTimeISO());
       
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString(); 
            logger.log(message);
        }
    }

    /**
     * Returns a registered {@link UserLogListener} instance.
     */
    public static ExtensionList<UserLogListener> getInstance() {
        return ExtensionList.lookup(UserLogListener.class);
    }

    private static String formatDateISO(long milliseconds) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault()));
    }

    private static String currentDateTimeISO() {
        return formatDateISO(System.currentTimeMillis());
    }
}