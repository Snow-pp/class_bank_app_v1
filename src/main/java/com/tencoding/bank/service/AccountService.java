package com.tencoding.bank.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tencoding.bank.dto.DepositFormDto;
import com.tencoding.bank.dto.HistoryDto;
import com.tencoding.bank.dto.SaveFormDto;
import com.tencoding.bank.dto.TransferFormDto;
import com.tencoding.bank.dto.WithdrawFormDto;
import com.tencoding.bank.handler.exception.CustomRestfulException;
import com.tencoding.bank.repository.interfaces.AccountRepository;
import com.tencoding.bank.repository.interfaces.HistoryRepository;
import com.tencoding.bank.repository.model.Account;
import com.tencoding.bank.repository.model.History;

@Service // IoC 대상 + 싱글톤 패턴 => 스프링 컨테이너 메모리에 객체가 생성
public class AccountService {

	@Autowired // DI - 가지고 오다
	private AccountRepository accountRepository;
	
	@Autowired // DI 처리
	private HistoryRepository historyRepository;
	
	@Transactional
	public void createAccount(SaveFormDto saveFormDto, Integer principalId) {
		// 등록 처리 - insert
		Account account = new Account();
		account.setNumber(saveFormDto.getNumber());
		account.setPassword(saveFormDto.getPassword());
		account.setBalance(saveFormDto.getBalance());
		account.setUserId(principalId);
		
		int resultRowCount = accountRepository.insert(account);
		if (resultRowCount != 1) {
			throw new CustomRestfulException("계좌 생성 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/*
	 * 계좌 목록 보기 (로그인 된 사용자)
	 * @param userId
	 * @return List<Account> 
	 */
	@Transactional
	public List<Account> readAccountList(Integer userId) {
		List<Account> list = accountRepository.findByUserId(userId);
		return list;
	}

	// 출금 기능 로직 고민해보기
	// 1. 계좌 존재 여부 확인 => select query
	// 2. 본인 계좌 여부 확인
	// 3. 계좌 비밀번호 확인
	// 4. 잔액 여부 확인
	// 5. 출금 처리 => update query
	// 6. 거래 내역 등록 => insert
	// 7. 트랜잭션 처리
	@Transactional
	public void updateAccountWithdraw(WithdrawFormDto withdrawFormDto, Integer id) {
		// 현재 모델 상태를 accountEnitity 객체에 담는다.
		Account accountEntity = accountRepository.findByNumber(withdrawFormDto.getWAccountNumber());
		if (accountEntity == null) { // 1. 존재 여부
			throw new CustomRestfulException("해당 계좌가 없습니다.", HttpStatus.BAD_REQUEST);
		}
		if (accountEntity.getUserId() != id) { // 2. 본인 계좌 여부
			throw new CustomRestfulException("본인 소유 계좌가 아닙니다.", HttpStatus.BAD_REQUEST);
		}
		// 3. 계좌 비밀번호
		if (accountEntity.getPassword().equals(withdrawFormDto.getWAccountPassword()) == false) {
			throw new CustomRestfulException("출금 계좌 비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST);
		}
		if (accountEntity.getBalance() < withdrawFormDto.getAmount()) { // 4. 잔액 확인
			throw new CustomRestfulException("계좌 잔액이 부족합니다.", HttpStatus.BAD_REQUEST);
		}
		// 5. update 쿼리 (모델 객체 상태 변경 --> 객체 다시 던지기)
		// 현재 모델 상태를 담은 accountEnitity에서 form에 입력된 출금금액을 출금한다.
		// 1300-300=1000. accountEnitity에는 변경된 금액이 저장되어 있다.
		accountEntity.withdraw(withdrawFormDto.getAmount());
		// 쿼리 던지기 (모델 accountRepository에 출금된 후 변경된 정보(accountEnitity)를 저장한다.)
		accountRepository.updateById(accountEntity);
		// 6. 거래내역 등록 History 객체 생성
		History history = new History();
		history.setAmount(withdrawFormDto.getAmount());
		// 출금 시점에 해당 계좌 잔액을 입력
		history.setWBalance(accountEntity.getBalance());
		history.setDBalance(null);
		history.setWAccountId(accountEntity.getId());
		history.setDAccountId(null);
		
		int resultRowCount = historyRepository.insert(history);
		if (resultRowCount != 1) {
			throw new CustomRestfulException("정상 처리되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	// 입금 기능 로직 생각해보기
	// 1. 계좌 존재 여부 확인 --> select query
	// 2. 입금 처리 --> update query
	// 3. 거래 내역 등록 --> insert query
	public void updateAccountDeposit(DepositFormDto depositFormDto) {
		Account accountEntity = accountRepository.findByNumber(depositFormDto.getDAccountNumber());
		if (accountEntity == null) {
			throw new CustomRestfulException("해당 계좌가 없습니다.", HttpStatus.BAD_REQUEST);
		}
		
		// 객체 상태값 변경 처리
		accountEntity.deposit(depositFormDto.getAmount()); 
		accountRepository.updateById(accountEntity); // update 처리
		
		// 거래 내역 등록
		History history = new History();
		history.setAmount(depositFormDto.getAmount());
		history.setWAccountId(null);
		history.setDAccountId(accountEntity.getId());
		history.setWBalance(null);
		history.setDBalance(accountEntity.getBalance());
		
		int resultRowCount = historyRepository.insert(history);
		if (resultRowCount != 1) {
			throw new CustomRestfulException("정상 처리되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// 이체 로직 고민해보기
	// 1. 출금 계좌 존재 여부 확인 - select
	// 2. 입금 계좌 존재 여부 확인 - select
	// 3. 출금 계좌 본인 소유 확인 - 객체 상태값 확인 (매개변수 id / 1번 select객체 id 비교)
	// 4. 출금 계좌 비밀번호 확인 - TransferFormDto(비밀번호) / 모델객체(비밀번호) 비교
	// 5. 출금 계좌 잔액 여부 확인 - Dto / 모델
	// 6. 출금 계좌 잔액 상태값 변경 처리 - update
	// 7. 입금 계좌 잔액 - update
	// 8. 거래 내역 등록
	// 9. 트랜잭션 처리
	@Transactional
	public void updateAccountTransfer(TransferFormDto transferFormDto, Integer id) {
		// 1
		Account withdrawAccountEntity = accountRepository.findByNumber(transferFormDto.getWAccountNumber());
		if (withdrawAccountEntity == null) {
			throw new CustomRestfulException("출금 계좌가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
		}
		// 2
		Account depositAccountEntity = accountRepository.findByNumber(transferFormDto.getDAccountNumber());
		if (depositAccountEntity == null) {
			throw new CustomRestfulException("입금 계좌가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
		}
		// 3. 출금 계좌 본인 소유 확인
		withdrawAccountEntity.checkOwner(id);
		// 4. 출금 계좌 비밀번호 확인
		withdrawAccountEntity.checkPassword(transferFormDto.getWAccountPassword());
		// 5. 출금 계좌 잔액 여부 확인
		withdrawAccountEntity.checkBalance(transferFormDto.getAmount());
		// 6. 출금 계좌 잔액 상태값 변경 처리
		withdrawAccountEntity.withdraw(transferFormDto.getAmount());
		// update 처리
		accountRepository.updateById(withdrawAccountEntity);
		// 7. 입금 계좌 잔액 상태값 변경 처리
		depositAccountEntity.deposit(transferFormDto.getAmount());
		// update 처리
		accountRepository.updateById(depositAccountEntity);
		// 8
		History history = new History();
		history.setAmount(transferFormDto.getAmount());
		history.setWAccountId(withdrawAccountEntity.getId());
		history.setDAccountId(depositAccountEntity.getId());
		history.setWBalance(withdrawAccountEntity.getBalance());
		history.setDBalance(depositAccountEntity.getBalance());
		
		int resultRowCount = historyRepository.insert(history);
		if (resultRowCount != 1) {
			throw new CustomRestfulException("정상 처리되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 단일 계좌 정보 검색
	 * @param id (계좌 pk)
	 * @return Account Entity
	 */
	public Account readAccount(Integer id) {
		// 계좌 존재 여부 확인
		Account accountEntity = accountRepository.findById(id);
		if (accountEntity == null) {
			throw new CustomRestfulException("해당 계좌를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
		}
		
		return accountEntity;
	}

	/**
	 * 단일 계좌에 대한 거래 내역 검색
	 * @param type = [all, deposit, withdraw]
	 * @param id(account pk)
	 * @return History 거래 내역 (DTO)
	 */
	public List<HistoryDto> readHistoryListByAccount(Integer id, String type) {
		List<HistoryDto> historyList = historyRepository.findByHistoryType(id, type);
		
		return historyList;
	}
}









