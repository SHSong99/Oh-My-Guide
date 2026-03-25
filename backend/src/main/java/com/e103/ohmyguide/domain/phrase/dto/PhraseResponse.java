package com.e103.ohmyguide.domain.phrase.dto;

import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.phrase.entity.PhraseLanguage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhraseResponse {

    private Long phraseId;
    private String content;
    private PhraseLanguage language;

    public static PhraseResponse from(Phrase phrase) {
        return PhraseResponse.builder()
                .phraseId(phrase.getId())
                .content(phrase.getContent())
                .language(phrase.getLanguage())
                .build();
    }
}
