package com.devember.devember.user.type;

import lombok.Getter;

@Getter
public enum UserErrorCode {

	USER_NOT_FOUND("유저를 찾을 수 없습니다."),
	DUPLICATED_USER("이미 가입되어 있는 유저입니다."),
	WRONG_USER_PASSWORD("잘못된 비밀번호입니다."),
	OVERLAP_STATUS("이미 계정의 상태는 해당 코드와 같습니다."),
	NOT_AUTHENTICATED("인증되지 않은 계정입니다.");

	private final String description;

	UserErrorCode(String description) {
		this.description = description;
	}
}
