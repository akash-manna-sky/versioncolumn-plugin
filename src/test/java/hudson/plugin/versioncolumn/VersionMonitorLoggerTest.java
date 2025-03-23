package hudson.plugin.versioncolumn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import hudson.model.Computer;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import jenkins.security.MasterToSlaveCallable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.LoggerRule;
import org.mockito.ArgumentMatchers;

public class VersionMonitorLoggerTest {

    @Rule
    public LoggerRule loggerRule = new LoggerRule();

    private VersionMonitor.DescriptorImpl descriptor;
    private Computer computer;
    private VirtualChannel channel;

    @Before
    public void setUp() {
        descriptor = spy(new VersionMonitor.DescriptorImpl());
        doReturn(false).when(descriptor).isIgnored(); // Not ignored

        computer = mock(Computer.class);
        channel = mock(VirtualChannel.class);

        // Configure the LoggerRule to capture messages from VersionMonitor
        loggerRule.record(VersionMonitor.class, Level.WARNING).capture(10);
    }

    @Test
    public void testLoggingWhenMarkingOffline() throws IOException, InterruptedException {
        // Arrange
        when(computer.getChannel()).thenReturn(channel);
        when(computer.getName()).thenReturn("TestAgent");
        when(channel.call(ArgumentMatchers.<MasterToSlaveCallable<String, IOException>>any()))
                .thenReturn("different-version");

        // Act
        descriptor.monitor(computer);

        // Assert
        assertTrue(
                "Log should contain agent name",
                loggerRule.getMessages().stream().anyMatch(msg -> msg.contains("TestAgent")));

        // Verify the log level is WARNING
        assertEquals(
                Level.WARNING.getName(),
                loggerRule.getRecords().stream()
                        .filter(record -> record.getMessage().contains("TestAgent"))
                        .findFirst()
                        .map(LogRecord::getLevel)
                        .map(Level::getName)
                        .orElse(null));
    }
}
