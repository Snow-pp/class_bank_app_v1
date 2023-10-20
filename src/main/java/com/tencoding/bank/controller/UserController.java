package com.tencoding.bank.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.tencoding.bank.dto.KakaoProfile;
import com.tencoding.bank.dto.OAuthToken;
import com.tencoding.bank.dto.SignInFormDto;
import com.tencoding.bank.dto.SignUpFormDto;
import com.tencoding.bank.handler.exception.CustomRestfulException;
import com.tencoding.bank.repository.model.User;
import com.tencoding.bank.service.UserService;
import com.tencoding.bank.utils.Define;

@Controller // 어노테이션 주의
@RequestMapping({"/user", "/"}) // 주소에 user가 공통적으로 들어가므로 자동으로 user로 가도록 설정
public class UserController {
	
	@Value("${tenco.key}")
	private String tencoKey;
	
	@Autowired // DI 처리
	private UserService userService;
	
	@Autowired // DI 처리
	private HttpSession session;
	
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
	@GetMapping({"/sign-in", ""})
	public String signIn() {
		return "user/signIn";
	}
	
	/**
	 * 회원 가입 처리
	 * @param signUpformDto
	 * @return 리다이렉트 처리 - 로그인 페이지 
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpFormDto signUpFormDto) {
		
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
		
		return "redirect:/user/sign-in";
	}
	
	/**
	 * 로그인 로직 처리
	 * @param signInFormDto
	 * @return 계좌 리스트 페이지로 리턴
	 * */
	@PostMapping("/sign-in")
	public String signInProc(SignInFormDto signInFormDto) {
		
		// 1. 유효성 검사
		if (signInFormDto.getUsername() == null || signInFormDto.getUsername().isEmpty()) {
			throw new CustomRestfulException("username을 입력하시오", HttpStatus.BAD_REQUEST);
		}
		if (signInFormDto.getPassword() == null || signInFormDto.getPassword().isEmpty()) {
			throw new CustomRestfulException("password을 입력하시오", HttpStatus.BAD_REQUEST);
		}
		
		// 2. 서비스 -> 인증된 사용자 여부 확인
		// principal <-- 접근 주체
		User principal = userService.signIn(signInFormDto);
		principal.setPassword(null);
		
		// 3. 쿠키 + 세션 
		session.setAttribute(Define.PRINCIPAL, principal);
		
		return "redirect:/account/list";
	}

	/**
	 * 로그아웃 처리
	 * 
	 * @return 리다이렉트 - 로그인 페이지로 이동
	 */
	@GetMapping("/logout")
	public String logout() {
		session.invalidate();
		return "redirect:/user/sign-in";
	}
	
	// http://localhost:80/user/kakao/callback?code="aaa"
	@GetMapping("/kakao/callback")
	// @ResponseBody // data 반환 명시
	public String kakaoCallback(@RequestParam String code) {
		System.out.println("메서드 동작");
		
		// PIST 방식 - exchange() 메서드 활용
		// Header 구성
		// body 구성
		
		RestTemplate rt = new RestTemplate();
		// Header 구성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		
		// body 구성
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", "37dedab7e90e57927dc7b3f16d9d65e1");
		params.add("redirect_uri", "http://localhost:80/user/kakao/callback");
		params.add("code", code);
		
		// HttpEntity 결합 (헤더 + 바디)
		HttpEntity<MultiValueMap<String, String>> reqMes = new HttpEntity<>(params, headers);
		
		// HTTP 요청
		ResponseEntity<OAuthToken> response = rt.exchange("https://kauth.kakao.com/oauth/token", HttpMethod.POST, reqMes, OAuthToken.class);
		
		// 1. DTO 파싱
		System.out.println("액세스 토큰 확인 " + response.getBody().toString());
		// 액세스 토큰
		// 액세스 토큰 --> 카카오 서버 (정보)
		
		// 문서 확인 - 정보 요청 주소, 형식 확인
		System.out.println("------------------------------------");
		RestTemplate ret2 = new RestTemplate();
		
		// 헤더 생성
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer " + response.getBody().getAccessToken());
		headers2.add("Content-type", "Content-type: application/x-www-form-urlencoded;charset=utf-8");
		// 바디 생성 - 생략
		// 결합
		HttpEntity<MultiValueMap<String, String>> kakaoInfo
			= new HttpEntity<>(headers2); // 바디 없이 헤더만 추가
		
		// HTTP 요청
		ResponseEntity<KakaoProfile> response2 = ret2.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST, 
														kakaoInfo, KakaoProfile.class);
		System.out.println("------------------------------------");
		System.out.println(response2.getBody().getKakaoAccount().getEmail());
		
		System.out.println("카카오 서버에 정보받기 완료");
		
		// 1. 회원 가입 여부 확인
		// 최초 사용자라면 우리 회원가입 맞는 형식을 만들어서 회원가입 처리
		// DB -> user_tb --> username, password, fullname
		// password -> 직접 만들어서 넣어야한다.
		// 소셜 로그인 사용자는 모든 패스워드가 동일하다.
		
		// username -> 동일한 값이 저장되지 않도록 처리해야한다.
		
		KakaoProfile kakaoProfile = response2.getBody();
		
		SignUpFormDto signUpFormDto = SignUpFormDto
				.builder()
				.username(kakaoProfile.getKakaoAccount().getEmail() + "_" + kakaoProfile.getId())
				.fullname("OAuth_" + kakaoProfile.getKakaoAccount().getEmail())
				.password(tencoKey)
				.build();
		
		// User, null
		User oldUser = userService.searchUsername(signUpFormDto.getUsername());
		if (oldUser == null) {
			// 사용자가 최초 소셜 로그인 사용자면 자동 회원가입 처리
			userService.signUp(signUpFormDto);
			oldUser.setUsername(signUpFormDto.getUsername());
			oldUser.setFullname(signUpFormDto.getFullname());
		}
		oldUser.setPassword(null);
		// 2. 로그인 처리 -> 세션 메모리에 사용자를 등록 (세션 생성)
		// 최초 사용자가 아니라면 바로 세션에 데이터 등록 로그인 처리
		session.setAttribute(Define.PRINCIPAL, oldUser); // 로그인 처리
		
		
		return "redirect:/account/list";
	}
}
