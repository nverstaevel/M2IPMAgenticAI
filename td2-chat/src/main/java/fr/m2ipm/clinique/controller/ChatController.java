package fr.m2ipm.clinique.controller;

import fr.m2ipm.clinique.ai.PropositionConsultation;
import fr.m2ipm.clinique.ai.PropositionService;
import fr.m2ipm.clinique.ai.TriageResultat;
import fr.m2ipm.clinique.dto.ConsultationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class ChatController {

    public record ChatRequest(@NotBlank(message = "conversationId est obligatoire") String conversationId,
                              @NotBlank(message = "message est obligatoire") String message) {}

    public record ChatReponse(String reponse, List<PropositionConsultation> propositions) {}

    public record TriageRequest(@NotBlank(message = "texte est obligatoire") String texte) {}

    private final ChatClient chat;
    private final PropositionService propositions;

    public ChatController(ChatClient chat, PropositionService propositions) {
        this.chat = chat;
        this.propositions = propositions;
    }

    @PostMapping("/chat")
    public ChatReponse chat(@Valid @RequestBody ChatRequest requete) {
        String reponse = chat.prompt()
                .user(requete.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, requete.conversationId()))
                .toolContext(Map.of("conversationId", requete.conversationId()))
                .call()
                .content();
        return new ChatReponse(reponse, propositions.enAttente(requete.conversationId()));
    }

    /** Même conversation en flux : Server-Sent Events, un fragment de texte par événement. */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> flux(@RequestParam String conversationId, @RequestParam String message) {
        return chat.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolContext(Map.of("conversationId", conversationId))
                .stream()
                .content();
    }

    /** Texte libre en entrée, record Java en sortie : le modèle remplit la structure. */
    @PostMapping("/triage")
    public TriageResultat triage(@Valid @RequestBody TriageRequest requete) {
        return chat.prompt()
                .system("Tu es l'infirmier de tri de la clinique des créatures fantastiques. "
                        + "Résume la demande et estime l'urgence.")
                .user(requete.texte())
                .call()
                .entity(TriageResultat.class);
    }

    @GetMapping("/propositions")
    public List<PropositionConsultation> propositions(@RequestParam String conversationId) {
        return propositions.enAttente(conversationId);
    }

    /** Réservé au rôle PERSONNEL (toute écriture POST sous /api l'exige). C'est ici, et nulle part côté modèle, qu'on écrit. */
    @PostMapping("/propositions/{id}/confirmer")
    public ConsultationDto confirmer(@PathVariable String id) {
        return propositions.confirmer(id);
    }
}
