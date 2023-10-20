package com.tencoding.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tencoding.bank.dto.SignInFormDto;
import com.tencoding.bank.dto.SignUpFormDto;
import com.tencoding.bank.repository.model.User;

// ibatis => 2.4 버전 이후로 MyBatis로 이름 변경됨
@Mapper // Mapper 반드시 기술을 해주어야 동작한다.
public interface UserRepository {
	// 뱅크 앱
//	public int insert(User user); => SignUpFormDto로 코드 수정
	public int insert(SignUpFormDto dto);
	public int updateById(User user); // Id 기반으로 update한다.
	public int deletdById(Integer id);
	public User findById();
	public List<User> findAll();
	public User findByUsernameAndPassword(SignInFormDto signInFormDto);
	public User findByUsername(String username);
}
