package fr.m2ipm.clinique.roles;

import fr.m2ipm.clinique.ai.OutilsClinique;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** L'acteur : dossier, pharmacie et tarifs. Deux profils de prompt. Répond par sortie structurée. */
@Component
public class VeterinaireAgent {

    static final String BASE = """
            [ROLE:VETERINAIRE]
            Tu es le vétérinaire de la clinique des créatures fantastiques. Tu proposes un diagnostic et un traitement
            pour la créature décrite, en français. Tu peux consulter le dossier, la pharmacie et les tarifs avec tes outils.
            Tu réponds uniquement par le Diagnostic demandé (créature, diagnostic, traitements, justification).
            Si le gardien du bestiaire a formulé des objections, tu en tiens compte et tu proposes un traitement différent.
            """;

    static final String PRUDENT = BASE + """
            Profil PRUDENT : avant de proposer, tu vérifies dans le catalogue les contre-indications pour l'espèce
            et tu choisis un traitement adapté.
            """;

    static final String PRESSE = BASE + """
            Profil PRESSÉ : tu proposes sans vérifier le premier remède qui te vient, celui qui est indiqué comme piste habituelle.
            """;

    static final String OBSTINE = BASE + """
            Profil OBSTINÉ : tu es convaincu de la piste habituelle indiquée. Même si le gardien formule des objections,
            tu maintiens le même traitement, en reformulant seulement la justification.
            """;

    private final ChatClient client;

    @Autowired
    public VeterinaireAgent(ChatClient.Builder builder, OutilsClinique clinique, OutilsPharmacie pharmacie) {
        this(builder.defaultToolCallbacks(fr.m2ipm.clinique.agent.SuiviOutils.envelopper(
                org.springframework.ai.support.ToolCallbacks.from(clinique, pharmacie)))
                .defaultAdvisors(fr.m2ipm.clinique.agent.ComptageJetons.advisor()).build());
    }

    public VeterinaireAgent(ChatClient client) { this.client = client; }

    public Diagnostic proposer(CasBestiaire cas, ProfilActeur profil, List<String> objections) {
        StringBuilder u = new StringBuilder();
        u.append("Cas ").append(cas.id()).append(" — créature : ").append(cas.creature()).append(" (").append(cas.espece())
                .append(") ; symptôme : ").append(cas.symptome()).append('\n');
        u.append("Profil : ").append(profil).append('\n');
        if (profil != ProfilActeur.PRUDENT) {
            u.append("Piste habituelle : ").append(cas.traitementPresse()).append('\n');
        }
        if (objections != null && !objections.isEmpty()) {
            u.append("Objections du gardien à prendre en compte : ").append(String.join(" ; ", objections)).append('\n');
        }
        return client.prompt()
                .system(switch (profil) { case PRESSE -> PRESSE; case OBSTINE -> OBSTINE; default -> PRUDENT; })
                .user(u.toString())
                .call()
                .entity(Diagnostic.class);
    }
}
