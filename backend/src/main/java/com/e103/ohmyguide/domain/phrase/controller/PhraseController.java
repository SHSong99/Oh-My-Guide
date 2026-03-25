package com.e103.ohmyguide.domain.phrase.controller;

import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.phrase.dto.PhraseResponse;
import com.e103.ohmyguide.domain.phrase.service.PhraseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/phrases")
@RequiredArgsConstructor
public class PhraseController {

    private final PhraseService phraseService;

    @GetMapping("/bookmarks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PhraseResponse>> getBookmarks(@CurrentUser UserPrincipal userPrincipal) {
        List<PhraseResponse> bookmarks = phraseService.getBookmarks(userPrincipal.getId());
        return ResponseEntity.ok(bookmarks);
    }

    @PostMapping("/{phraseId}/bookmark")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> addBookmark(@PathVariable Long phraseId, @CurrentUser UserPrincipal userPrincipal) {
        phraseService.addBookmark(phraseId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{phraseId}/bookmark")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long phraseId, @CurrentUser UserPrincipal userPrincipal) {
        phraseService.removeBookmark(phraseId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
