package ftp.client.spring.boot.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ftp.client.spring.boot.exception.FTPErrors;
import ftp.client.spring.boot.service.IFTPService;

@RestController
@RequestMapping("/report")
public class FTPController {

	@Value("${ftp.file.extension}")
	private String extension;

	@Autowired
	private IFTPService ftpService;
	
	// http://localhost:8080/report/upload-file?name=caramelo
	@GetMapping("/upload-file")
	public ResponseEntity<InputStreamResource> uploadFileToFTP(@RequestParam(required = true) String name)
			throws IOException {
		String path = name + extension;
		File file = new File(path);
		BufferedWriter bw;
		if (file.exists()) {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write("El fichero de texto ya estaba creado.");
		} else {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write("Acabo de crear el fichero de texto.");
		}
		bw.close();

		try {
			ftpService.uploadFileToFTP(file);
		} catch (FTPErrors e) {
			e.printStackTrace();
		}

		return null;
	}

	// http://localhost:8080/report/download-file?name=caramelo
	@GetMapping("/download-file")
	public ResponseEntity<InputStreamResource> downloadFileToFTP(@RequestParam(required = true) String name) {
		String fileName = name + extension;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=" + fileName);

		InputStreamResource inputStreamResource;
		try {
			inputStreamResource = ftpService.downloadFileToFTP(fileName);
			return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(inputStreamResource);
		} catch (FTPErrors e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}
	}

}
