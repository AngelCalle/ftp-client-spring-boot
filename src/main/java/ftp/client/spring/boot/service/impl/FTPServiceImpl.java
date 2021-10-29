package ftp.client.spring.boot.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import ftp.client.spring.boot.exception.ErrorMessage;
import ftp.client.spring.boot.exception.FTPErrors;
import ftp.client.spring.boot.service.IFTPService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FTPServiceImpl implements IFTPService {

	@Value("${ftp.host}")
	private String host;

	@Value("${ftp.port}")
	private Integer port;

	@Value("${ftp.user}")
	private String user;

	@Value("${ftp.password}")
	private String password;

	private FTPClient ftpconnection;

	@Override
	public void connectFTP(String host, Integer port, String user, String password) throws FTPErrors {
		ftpconnection = new FTPClient();
		ftpconnection.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		int reply;

		try {
			ftpconnection.connect(host, port);
		} catch (IOException e) {
			ErrorMessage errorMessage = new ErrorMessage(-1, "Could not connect to FTP via host =" + host + ".");
			log.error(errorMessage.toString());
			throw new FTPErrors(errorMessage);
		}

		reply = ftpconnection.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			try {
				ftpconnection.disconnect();
			} catch (IOException e) {
				ErrorMessage errorMessage = new ErrorMessage(-2,
						"Could not connect to FTP, host =" + host + " delivered the answer =" + reply + ".");
				log.error(errorMessage.toString());
				throw new FTPErrors(errorMessage);
			}
		}

		try {
			ftpconnection.login(user, password);
		} catch (IOException e) {
			ErrorMessage errorMessage = new ErrorMessage(-3,
					"User=" + user + ", and the passwords=**** they were not valid for authentication.");
			log.error(errorMessage.toString());
			throw new FTPErrors(errorMessage);
		}

		try {
			ftpconnection.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			ErrorMessage errorMessage = new ErrorMessage(-4, "The data type for the transfer is invalid.");
			log.error(errorMessage.toString());
			throw new FTPErrors(errorMessage);
		}

		ftpconnection.enterLocalPassiveMode();
	}

	@Override
	public void disconnectFTP() throws FTPErrors {
		if (this.ftpconnection.isConnected()) {
			try {
				this.ftpconnection.logout();
				this.ftpconnection.disconnect();
			} catch (IOException f) {
				throw new FTPErrors(new ErrorMessage(-5, "An error occurred while disconnecting from the FTP server."));
			}
		}
	}

	@Override
	public void uploadFileToFTP(File file) throws FTPErrors {
		String ftpHostDir = "\\";
		String serverFilename = file.getName();
		try {
			this.connectFTP(host, port, user, password);
			InputStream input = new FileInputStream(file);
			this.ftpconnection.storeFile(ftpHostDir + serverFilename, input);
			this.disconnectFTP();
		} catch (IOException e) {
			ErrorMessage errorMessage = new ErrorMessage(-7, "Could not upload file to server.");
			log.error(errorMessage.toString());
			throw new FTPErrors(errorMessage);
		}
	}
	
	@Override
	public InputStreamResource downloadFileToFTP(String fileName) throws FTPErrors {
		try {
			this.connectFTP(host, port, user, password);
			InputStream inputStream = this.ftpconnection.retrieveFileStream(fileName);
			InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
			this.disconnectFTP();
			return inputStreamResource;
		} catch (IOException e) {
			ErrorMessage errorMessage = new ErrorMessage(-6, "The file could not be downloaded.");
			log.error(errorMessage.toString());
			throw new FTPErrors(errorMessage);
		}
	}

}
