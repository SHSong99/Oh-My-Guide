package com.e103.ohmyguide.domain.phrase.service;

import com.e103.ohmyguide.domain.phrase.dto.PhraseResponse;
import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.phrase.repository.PhraseRepository;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.userphrase.entity.UserPhrase;
import com.e103.ohmyguide.domain.userphrase.repository.UserPhraseRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PhraseService {

    private final PhraseRepository phraseRepository;
    private final UserRepository userRepository;
    private final UserPhraseRepository userPhraseRepository;

    public List<PhraseResponse> getBookmarks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userPhraseRepository.findByUser(user).stream()
                .map(userPhrase -> PhraseResponse.from(userPhrase.getPhrase()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addBookmark(Long phraseId, Long userId) {

        /** 예외 의논 후 커스텀 예외 및 advice 생성해야할 듯 */
        Phrase phrase = phraseRepository.findById(phraseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phrase", "id", phraseId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 중복 북마크 체크
        if (userPhraseRepository.existsByUserAndPhrase(user, phrase)) {
            return; // 이미 북마크되어 있으면 무시
        }

        UserPhrase userPhrase = UserPhrase.builder()
                .user(user)
                .phrase(phrase)
                .build();

        userPhraseRepository.save(userPhrase);
    }

    @Transactional
    public void removeBookmark(Long phraseId, Long userId) {
        Phrase phrase = phraseRepository.findById(phraseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phrase", "id", phraseId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userPhraseRepository.deleteByUserAndPhrase(user, phrase);
    }
}
