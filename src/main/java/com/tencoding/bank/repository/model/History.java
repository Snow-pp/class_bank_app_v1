package com.tencoding.bank.repository.model;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class History {
	private Integer id;
	private Long amouLong;
	private Long wBalance;
	private Long dBalance;
	private Integer wAccountId;
	private Integer dAccountId;
	private Timestamp createdAt;
}
