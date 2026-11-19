package fr.m2ipm.clinique;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/** Vérifie le câblage des vrais modèles sans serveur : aucun appel réseau au démarrage. */
public class ChoixDuModeleTest {

    @SpringBootTest
    static class ParDefaut {
        @Autowired ApplicationContext contexte;

        @Test
        void ollamaEstLeModeleParDefaut() {
            Map<String, ChatModel> modeles = contexte.getBeansOfType(ChatModel.class);
            assertThat(modeles).hasSize(1);
            assertThat(modeles.values().iterator().next().getClass().getSimpleName()).isEqualTo("OllamaChatModel");
        }
    }

    @SpringBootTest
    @ActiveProfiles("lmstudio")
    static class AvecLmStudio {
        @Autowired ApplicationContext contexte;

        @Test
        void leProfilLmstudioActiveLeModeleCompatibleOpenAi() {
            Map<String, ChatModel> modeles = contexte.getBeansOfType(ChatModel.class);
            assertThat(modeles).hasSize(1);
            assertThat(modeles.values().iterator().next().getClass().getSimpleName()).isEqualTo("OpenAiChatModel");
        }
    }
}
