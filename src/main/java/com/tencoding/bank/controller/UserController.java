package com.tencoding.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tencoding.bank.dto.SignUpFormDto;
import com.tencoding.bank.handler.exception.CustomRestfulException;
import com.tencoding.bank.service.UserService;

@Controller // 어노테이션 주의
@RequestMapping("/user") // 주소에 user가 공통적으로 들어가므로 자동으로 user로 가도록 설정
public class UserController {
	
	@Autowired
	private UserService userService;
	
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
	
	/*
	 * 회원 가입 처리
	 * @param signUpformDto
	 * @return 리다이렉트 처리 - 로그인 페이지 
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpFormDto signUpFormDto) {
		System.out.println("signUpFormDto: " + signUpFormDto);
		System.out.println("========== signUpProc 메서드 시작 ==========");
		// 1. 유효성 검사
		if (signUpFormDto.getUsername() == null || signUpFormDto.getUsername().isEmpty()) {
			throw new CustomRestfulException("username을 입력하세요", HttpStatus.BAD_REQUEST);
		}
		if (signUpFormDto.getPassword() == null || signUpFormDto.getPassword().isEmpty()) {
			throw new CustomRestfulException("password을 입력하세요", HttpStatus.BAD_REQUEST);
		}
		if (signUpFormDto.getFullname() == null || signUpFormDto.getFullname().isEmpty()) {
			throw new CustomRestfulException("fullname을 입력하세요", HttpStatus.BAD_REQUEST);
		}
		
		// 로직 추가 - 서비스 호출
		userService.signUp(signUpFormDto);
		System.out.println("========== signUpProc 메서드 끝 ==========");
		
		return "redirect:/user/sign-in";
	}
}
