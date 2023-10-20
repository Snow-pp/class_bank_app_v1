package com.tencoding.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignUpFormDto {
	private String username; // 화면 name 태그 기준
	private String password;
	private String fullname;
	
	// TODO - 추후 추가 예정
	
}
