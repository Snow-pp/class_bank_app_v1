<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

	<%@ include file="/WEB-INF/view/layout/header.jsp" %>
	<!-- header.jsp -->
	
	<!-- html 디자인 -->
	<!-- start main.jsp -->
    <div class="col-sm-8">
      <h2>로그인 페이지</h2>
      <h5>어서오세요 환영합니다</h5>
      <div class="bg-light p-md-5 h-75">
      	<!-- 로그인 보안 때문에 예외적으로 post 방식을 활용한다 -->
      	<form action="/user/sign-in" method="post">
      		<div class="form-group">
      			<label for="username">Username:</label>
      			<input type="text" id="username" class="form-control" placeholder="Enter username" name="username" value="길동">
      		</div>
      		<div class="form-group">
      			<label for="pwd">Password:</label>
      			<input type="password" id="pwd" class="form-control" placeholder="Enter password" name="password" value="1234">
      		</div>
      		<button type="submit" class="btn btn-primary">Submit</button>
      		<a href="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=37dedab7e90e57927dc7b3f16d9d65e1&redirect_uri=http://localhost:80/user/kakao/callback"><img src="/images/kakao_login_small.png" width="74" height="38"></a>
      	</form>
      </div>
      
      
    </div>
  </div>
</div>
<!-- end of main.jsp -->
	
	
	<!-- footer.jsp -->
	<%@ include file="/WEB-INF/view/layout/footer.jsp" %>

