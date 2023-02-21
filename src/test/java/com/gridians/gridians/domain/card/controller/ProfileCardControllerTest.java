package com.gridians.gridians.domain.card.controller;

import com.gridians.gridians.domain.card.entity.ProfileCard;
import com.gridians.gridians.domain.card.repository.ProfileCardRepository;
import com.gridians.gridians.domain.card.service.ProfileCardService;
import com.gridians.gridians.domain.user.entity.User;
import com.gridians.gridians.domain.user.exception.UserException;
import com.gridians.gridians.domain.user.repository.UserRepository;
import com.gridians.gridians.domain.user.service.UserService;
import com.gridians.gridians.global.config.security.filter.JwtAuthenticationFilter;
import com.gridians.gridians.global.config.security.service.CustomUserDetailsService;
import com.gridians.gridians.global.config.security.userdetail.JwtUserDetails;
import com.gridians.gridians.global.error.exception.ErrorCode;
import com.gridians.gridians.global.utils.JwtUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(ProfileCardController.class)
public class ProfileCardControllerTest {


	//Controller API를 테스트하기 위한 객체
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	UserRepository userRepository;

	@MockBean
	ProfileCardRepository profileCardRepository;

	//Bean으로 등록할 구현체를 넣어야 함
	@MockBean
	ProfileCardService profileCardService;

	@MockBean
	UserService userService;

	@MockBean
	PasswordEncoder passwordEncoder;

	@MockBean
	JwtUtils jwtUtils;

	@MockBean
	CustomUserDetailsService customUserDetailsService;

	@MockBean
	AuthenticationManager authenticationManager;

	@MockBean
	JwtAuthenticationFilter jwtAuthenticationFilter;

	String email = "dlwodud821@gmail.com";
	String password = "password12!";

	String accessToken;
	ProfileCard profileCard;

	@Test
	public void test() {

		when(jwtUtils.createAccessToken((JwtUserDetails) customUserDetailsService.loadUserByUsername(email)))
				.thenReturn("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkMDhjOWRmYS1jZmMzLTRmNWYtYTk2ZC04ZDA4MjU1YWVlMGMiLCJlbWFpbCI6ImVtYWlsQGVtYWlsLmNvbSIsInJvbGUiOiJbUk9MRV9VU0VSXSIsImlhdCI6MTY3Njk1MzY0MSwiZXhwIjoxNjc2OTU1NDQxfQ.MmIlgZIYRDkbxOxxtWYuEFyroqz9AvcZft70GfPv6rmpbhQiAB9FV41UeZgyqBFZVjXNrbWoJX0nGqg9q2heLQ");
		accessToken = jwtUtils.createAccessToken((JwtUserDetails) customUserDetailsService.loadUserByUsername(email));

	}

	@Test
	public void create() throws Exception {
		mockMvc.perform(post("/cards")
				.with(csrf())
				.header("Authorization", "Bearer " + accessToken));


	}


}