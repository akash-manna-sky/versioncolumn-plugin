package hudson.plugin.versioncolumn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import hudson.model.Computer;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jenkins.security.MasterToSlaveCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class VersionMonitorLoggerTest {

    private VersionMonitor.DescriptorImpl descriptor;
    private Computer computer;
    private VirtualChannel channel;
    private Logger logger;
    private TestLogHandler handler;

    @BeforeEach
    void setUp() {
        descriptor = spy(new VersionMonitor.DescriptorImpl());
        doReturn(false).when(descriptor).isIgnored(); // Not ignored

        computer = mock(Computer.class);
        channel = mock(VirtualChannel.class);

        // Set up logger to capture log messages
        logger = Logger.getLogger(VersionMonitor.class.getName());
        handler = new TestLogHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
    }

    @Test
    void testLoggingWhenMarkingOffline() throws IOException, InterruptedException {
        when(computer.getChannel()).thenReturn(channel);
        when(computer.getName()).thenReturn("TestAgent");
        when(channel.call(ArgumentMatchers.<MasterToSlaveCallable<String, IOException>>any()))
                .thenReturn("different-version");

        descriptor.monitor(computer);

        // Verify the log message contains the agent name
        assertTrue(handler.getMessage().contains("TestAgent"), "Log should contain agent name");
        assertEquals(Level.WARNING, handler.getLevel());
    }

    // Custom log handler to capture logs
    private static class TestLogHandler extends Handler {
        private LogRecord record;

        @Override
        public void publish(LogRecord record) {
            this.record = record;
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}

        public String getMessage() {
            return record.getMessage();
        }

        public Level getLevel() {
            return record.getLevel();
        }
    }
}
