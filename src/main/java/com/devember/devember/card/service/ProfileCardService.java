package com.devember.devember.card.service;

import com.devember.devember.card.dto.GithubDto;
import com.devember.devember.card.dto.ProfileCardDto;
import com.devember.devember.card.dto.ProfileCardDto.SnsDto;
import com.devember.devember.card.entity.*;
import com.devember.devember.card.exception.CardException;
import com.devember.devember.card.repository.*;
import com.devember.devember.card.type.CardErrorCode;
import com.devember.devember.user.entity.User;
import com.devember.devember.user.exception.UserException;
import com.devember.devember.user.repository.UserRepository;
import com.devember.devember.user.type.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileCardService {

	private final ProfileCardRepository profileCardRepository;
	private final UserRepository userRepository;
	private final GithubRepository githubRepository;
	private final SkillRepository skillRepository;
	private final SnsRepository snsRepository;
	private final TagRepository tagRepository;
	private final FieldRepository fieldRepository;
	private final ProfileCardSkillRepository profileCardSkillRepository;

	@Transactional
	public void createProfileCard(String email) {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		if (profileCardRepository.findByUser(user).isPresent()) {
			throw new UserException(UserErrorCode.DUPLICATED_USER);
		}
		ProfileCard pc = new ProfileCard();
		pc.setUser(user);
		profileCardRepository.save(pc);
	}

	@Transactional
	public void input(Long id, ProfileCardDto.updateRequest request) {
		ProfileCard pc = profileCardRepository.findById(id)
				.orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));

		saveField(pc, request);
		saveSnsSet(pc, request);
		saveSkillSet(pc, request);
		saveTagSet(pc, request);
		pc.setStatusMessage(request.getStatusMessage());

		profileCardRepository.save(pc);
	}

	@Transactional
	public ProfileCardDto.ReadResponse readProfileCard(Long id) {

		ProfileCard pc = profileCardRepository.findById(id)
				.orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));

		return ProfileCardDto.ReadResponse.from(
				pc.getStatusMessage(),
				pc.getField(),
				pc.getProfileCardSkillSet(),
				pc.getSnsSet(),
				pc.getTagList()
		);
	}

	@Transactional
	public void saveField(ProfileCard pc, ProfileCardDto.updateRequest request) {
		pc.setField(fieldRepository.findByName(request.getField()).orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND)));
	}

	@Transactional
	public void saveSnsSet(ProfileCard pc, ProfileCardDto.updateRequest request) {

		snsRepository.deleteAllInBatch(pc.getSnsSet());
		Set<SnsDto> sSet = request.getSnsSet();
		for (SnsDto snsDto : sSet) {
			pc.addSns(Sns.from(pc, snsDto.getName(), snsDto.getAccount()));
		}
	}

	@Transactional
	public void saveTagSet(ProfileCard pc, ProfileCardDto.updateRequest request) {

		tagRepository.deleteAllInBatch(pc.getTagList());
		Set<String> requestTagSet = request.getTagSet();

		for (String s : requestTagSet) {
			pc.addTag(Tag.from(s));
		}
	}

	@Transactional
	public void saveSkillSet(ProfileCard pc, ProfileCardDto.updateRequest request) {
		profileCardSkillRepository.deleteAllInBatch(pc.getProfileCardSkillSet());

		Set<String> sList = request.getSkillSet();
		Set<ProfileCardSkill> profileCardSkillList = new HashSet<>();

		for (String s : sList) {
			Skill skill = skillRepository.findByName(s).orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));
			pc.addProfileCardSkill(ProfileCardSkill.from(pc, skill));
		}
		profileCardRepository.save(pc);
	}

	@Transactional
	public void deleteProfileCard(Long id) {
		profileCardRepository.deleteById(id);
	}

	@Transactional
	public void saveGithub(GithubDto.Request request) throws IOException, ParseException, java.text.ParseException {
		ProfileCard pc = profileCardRepository.findById(request.getProfileCardId()).orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));
		Optional<Github> findGithub = githubRepository.findByProfileCard(pc);
		if (findGithub.isPresent()) {
			Github github = findGithub.get();
			github.setAll(parsing(request.getGithubId()));
		} else {
			Github github = Github.from(parsing(request.getGithubId()));
			github.setProfileCard(pc);
//			githubRepository.save(github);
			pc.setGithub(github);
		}
	}

	public void deleteGithub(GithubDto.Request request) throws IOException, ParseException, java.text.ParseException {
		ProfileCard pc = profileCardRepository.findById(request.getProfileCardId()).orElseThrow(() -> new CardException(CardErrorCode.CARD_NOT_FOUND));
		Github github = pc.getGithub();
		githubRepository.delete(github);
	}

	public GithubDto parsing(String githubId) throws IOException, ParseException, java.text.ParseException {
		System.out.println(githubId);
		JSONParser parser = new JSONParser();

		URL mainUrl = new URL("https://api.github.com/users/" + githubId);

		BufferedReader br = new BufferedReader(new InputStreamReader(mainUrl.openStream(), StandardCharsets.UTF_8));
		String result = br.readLine();
		JSONObject o1 = (JSONObject) parser.parse(result);

		System.out.println(o1.toString());


		URL subUrl = new URL("https://api.github.com/users/" + githubId + "/events");
		BufferedReader subBr = new BufferedReader(new InputStreamReader(subUrl.openStream(), StandardCharsets.UTF_8));
		String subResult = subBr.readLine();

		JSONArray jsonArray = (JSONArray) parser.parse(subResult);
		String message = "";
		String date = "";


		for (Object o : jsonArray) {
			JSONObject o2 = (JSONObject) o;

			if (o2.get("type").equals("PushEvent")) {
				date = (String) o2.get("created_at");
				Object payload = o2.get("payload");
				JSONObject payload1 = (JSONObject) payload;
				Object commits = payload1.get("commits");
				JSONArray commits1 = (JSONArray) commits;

				if (commits1.size() > 0) {
					Object o3 = commits1.get(0);
					JSONObject o31 = (JSONObject) o3;
					message = (String) o31.get("message");
					break;
				}
			}
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		LocalDate realDate = simpleDateFormat.parse(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		return GithubDto.builder()
				.name((String) o1.get("name"))
				.login((String) o1.get("login"))
				.githubId((Long) o1.get("id"))
				.githubUrl((String) o1.get("url"))
				.following((Long) o1.get("following"))
				.followers((Long) o1.get("followers"))
				.location((String) o1.get("location"))
				.imageUrl((String) o1.get("avatar_url"))
				.recentCommitAt(realDate)
				.recentCommitMessage(message)
				.build();
	}
}