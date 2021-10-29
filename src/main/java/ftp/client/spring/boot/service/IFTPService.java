package ftp.client.spring.boot.service;

import java.io.File;

import org.springframework.core.io.InputStreamResource;

import ftp.client.spring.boot.exception.FTPErrors;

public interface IFTPService {

     void connectFTP(String host, Integer port, String user, String password) throws FTPErrors;

     void disconnectFTP() throws FTPErrors;

     void uploadFileToFTP(File file) throws FTPErrors;

     InputStreamResource downloadFileToFTP(String string) throws FTPErrors;

}
