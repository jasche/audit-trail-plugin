package hudson.plugins.audit_trail;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.User;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

import javax.inject.Inject;

import java.io.File;
import java.util.Hashtable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Extension
public class SaveableChangeListener extends SaveableListener {

    @Inject
    AuditTrailPlugin configuration;

    /**
     * Fired when a saveable object is created. 
     *
     * @param o the saveable object.
     * @param file the XmlFile for this saveable object.
     */
    @Override
    public void onChange(Saveable o, XmlFile xml) {

        StringBuilder builder = new StringBuilder(100);
        User u = User.current();
        if(u == null) {
            return;
        }
        if("SYSTEM".equals(u.getDisplayName())) {
            return;
        }
        File f = xml.getFile();
        builder.append("config » " + f.getName() + " changed by " + u.getDisplayName() + " at " + currentDateTimeISO()); 
        
        for (AuditLogger logger : configuration.getLoggers()) {
            String message = builder.toString();
            logger.log(message);
        }
    }
    private static String formatDateISO(long milliseconds) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault()));
    }

    private static String currentDateTimeISO() {
        return formatDateISO(System.currentTimeMillis());
    }
}
