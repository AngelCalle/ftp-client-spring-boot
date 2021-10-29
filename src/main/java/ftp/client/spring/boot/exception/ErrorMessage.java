package ftp.client.spring.boot.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ErrorMessage extends Exception {

	private static final long serialVersionUID = 1L;

	private int code;

	private String message;

}
