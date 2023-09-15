package com.tencoding.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tencoding.bank.dto.SignUpFormDto;

@Controller // 어노테이션 주의
@RequestMapping("/user") // 주소에 user가 공통적으로 들어가므로 자동으로 user로 가도록 설정
public class UserController {
	// 회원 가입 페이지 요청
	// http://localhost:80/user/sign-up
	@GetMapping("/sign-up")
	public String signUp() {
		// /WEB-INF/view/ => yml파일에 prefix
		// /WEB-INF/view/user/signUp
		// .jsp => yml파일에 suffix
		return "user/signUp";
	}
	
	// 로그인 페이지 요청
	// http://localhost:80/user/sign-in
	// Get방식일 때 실행
	@GetMapping("/sign-in")
	public String signIn() {
		return "user/signIn";
	}
	
	// 회원 가입 처리
	// http://localhost:80/user/sign-up
	// POST - HTTP body (데이터)
	// name 속성을 이용해서 key=value 구조로 데이터 파싱
	// Object Mapper
	@PostMapping("/sign-up")
	public String signUpProc(SignUpFormDto signUpFormDto) {
		System.out.println("signUpFormDto: " + signUpFormDto);
		// 1. 유효성 검사
		// 2. 사용자 이미지 ..
		// 3. 서비스 호출
		// 4. 정상 처리 되었다면
		return "redirect:/user/sign-in";
	}
}
