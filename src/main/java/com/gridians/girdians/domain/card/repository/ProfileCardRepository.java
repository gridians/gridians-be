package com.gridians.girdians.domain.card.repository;

import com.gridians.girdians.domain.card.entity.ProfileCard;
import com.gridians.girdians.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileCardRepository extends JpaRepository<ProfileCard, Long> {

	Optional<ProfileCard> findByUser(User user);

}