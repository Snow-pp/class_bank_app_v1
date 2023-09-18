package com.tencoding.bank.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.tencoding.bank.handler.exception.CustomPageException;

/*
 * view 렌더링을 위해 ModelAndView
 * 객체를 반환하도록 설계할 때 사용
 * 예외 page를 리턴하도록 설계
 */
@ControllerAdvice // Ioc 대상
public class MyPageExceptionHandler {
	@ExceptionHandler(Exception.class)
//	프로젝트 진행하는 동안 계속 아래 예외 확인 계속 사용할 것
	public void exception(Exception e) {
		System.out.println("==== 예외 발생 확인 ====");
		System.out.println(e.getMessage());
		System.out.println("------------------------");
	}
	
	// 사용자 정의 클래스 활용
	@ExceptionHandler(CustomPageException.class)
	public ModelAndView handleRuntimePageException(CustomPageException e) {
		System.out.println("handleRuntimePageException 메서드");
		// ModelAndView 활용 방법 - 페이지 명시해야한다.
		ModelAndView modelAndView = new ModelAndView("errorPage"); // 페이지 명시
		modelAndView.addObject("statusCode", HttpStatus.NOT_FOUND.value());
		modelAndView.addObject("message", e.getMessage());
		return modelAndView;
	}
}
