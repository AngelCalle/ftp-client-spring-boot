package ftp.client.spring.boot.configuration;

import java.io.File;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway.Option;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.messaging.MessageChannel;

@Configuration
public class FtpConfiguration {
	
	@Bean
	public DefaultFtpSessionFactory sf() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost("localhost");
		sf.setPort(2121);
		sf.setUsername("anonymous");
		sf.setPassword("21232F297A57A5A743894A0E4A801FC3");
		return sf;
	}

	@ServiceActivator(inputChannel = "ftpLS")
	@Bean
	public FtpOutboundGateway getGW() {
		FtpOutboundGateway gateway = new FtpOutboundGateway(sf(), "ls", "payload");	
		gateway.setOption(Option.NAME_ONLY);
		gateway.setOutputChannelName("results");
		return gateway;
	}
	
	@MessagingGateway(defaultRequestChannel = "ftpLS", defaultReplyChannel = "results")
	public interface Gate {
		List<String> list(String directory);
	}

	@ServiceActivator(inputChannel = "ftpLS")
	@Bean
	public FtpOutboundGateway getFiles() {
	    FtpOutboundGateway gateway = new FtpOutboundGateway(sf(), "ls", "payload");
	    gateway.setAutoCreateDirectory(true);
	    gateway.setLocalDirectory(new File("./downloads/"));
	    gateway.setFileExistsMode(FileExistsMode.REPLACE_IF_MODIFIED);
	    gateway.setFilter(new AcceptOnceFileListFilter<>());
	    gateway.setOutputChannelName("results");
	    return gateway;
	}
	
	@MessagingGateway(defaultRequestChannel = "ftpLS", defaultReplyChannel = "results")
	public interface GateFile {
		List<File> ls(String directory);
	}

	@Bean
	public MessageChannel results() {
		DirectChannel channel = new DirectChannel();
		channel.addInterceptor(tap());
		return channel;
	}

	@Bean
	public WireTap tap() {
		return new WireTap("logging");
	}
	
	@ServiceActivator(inputChannel = "logging")
	@Bean
	public LoggingHandler logger() {
		LoggingHandler logger = new LoggingHandler(Level.INFO);
		logger.setLogExpressionString("'\n\nLogger Files:' + payload + '\n'");
		return logger;
	}

}
