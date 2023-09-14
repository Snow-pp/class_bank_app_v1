package com.tencoding.bank.repository.model;

import java.sql.Timestamp;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter // lombok 사용하여 Getter 생성
@Setter
@Data // Getter, Setter 한번에 생성
public class User {
	private Integer id;
	private String username;
	private String password;
	private String fullname;
	private Timestamp createdAt;
}
