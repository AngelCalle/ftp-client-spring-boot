package ftp.client.spring.boot.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class FTPErrors extends Exception {

	private static final long serialVersionUID = 1L;

	private ErrorMessage errorMessage;

}
