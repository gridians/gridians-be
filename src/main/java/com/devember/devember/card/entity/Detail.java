package com.devember.devember.card.entity;

import com.devember.devember.card.dto.ProfileCardDto;
import com.devember.devember.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Detail extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String status;
	@Column(name = "status_message")
	private String statusMessage;

	@OneToOne(mappedBy = "detail")
	private ProfileCard profileCard;


	public static Detail from(ProfileCardDto.DetailRequest request){
		return Detail.builder()
				.status(request.getStatus())
				.statusMessage(request.getStatusMessage())
				.build();
	}
}
