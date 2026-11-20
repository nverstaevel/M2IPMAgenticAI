package fr.m2ipm.clinique.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import fr.m2ipm.clinique.agent.Evenement;
import fr.m2ipm.clinique.agent.SuiviExecution;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ScenarioTest {

    private static ScenarioDef def(String md) { return ScenarioLoader.lire(md, "test", "test.md"); }

    @Test
    void fichierSansEnTeteEstSignale() {
        assertThat(def("pas d'en-tête").erreurs()).isNotEmpty();
        assertThat(def("---\nid: [\n---\ntexte").erreurs()).isNotEmpty();
    }

    @Test
    void lectureDuFrontMatter() {
        var d = def("---\nid: demo\ntitre: Démo\narchitecture: sequence\nagents:\n  etapes: [a, b]\n---\nCorps");
        assertThat(d.erreurs()).isEmpty();
        assertThat(d.id()).isEqualTo("demo");
        assertThat(d.description()).isEqualTo("Corps");
        assertThat(Orchestrateur.liste(d.agents().get("etapes"))).containsExactly("a", "b");
    }

    @Test
    void validationSignaleAgentInconnu() {
        var o = new Orchestrateur(Map.of(), new SuiviExecution());
        var d = def("---\nid: x\narchitecture: sequence\nagents:\n  etapes: [absent]\n---\n");
        assertThat(o.valider(d)).anyMatch(m -> m.contains("absent"));
        assertThat(o.valider(def("---\nid: x\narchitecture: bidon\n---\n"))).anyMatch(m -> m.contains("inconnue"));
    }

    @Test
    void boucleCritiqueSArreteALApprobation() {
        var suivi = new SuiviExecution();
        AtomicInteger appels = new AtomicInteger();
        Agent prod = e -> Resultat.texte("v" + e.tour());
        Agent crit = e -> appels.incrementAndGet() < 2 ? Resultat.avis(Decision.REJETE, List.of("trop court")) : Resultat.avis(Decision.APPROUVE, List.of());
        var o = new Orchestrateur(Map.of("p", prod, "c", crit), suivi);
        var d = def("---\nid: b\narchitecture: boucle-critique\nagents:\n  producteur: p\n  critique: c\noptions:\n  maxTours: 3\n  validationHumaine: false\n---\n");
        assertThat(o.valider(d)).isEmpty();
        o.executer("run-b", d, Map.of());
        List<Evenement> ev = suivi.evenements("run-b").orElseThrow();
        assertThat(ev).extracting(Evenement::id).contains("p1", "c1", "p2", "c2", "fin").doesNotContain("p3");
        assertThat(suivi.termine("run-b")).isTrue();
    }

    @Test
    void superviseurExecuteLesTachesDuPlan() {
        var suivi = new SuiviExecution();
        Agent plan = e -> Resultat.plan(List.of(new Tache("A", "fais a"), new Tache("B", "fais b")));
        Agent w = e -> Resultat.texte("ok " + e.texte());
        Agent syn = e -> Resultat.texte("synthèse : " + e.proposition().texte());
        var o = new Orchestrateur(Map.of("plan", plan, "w", w, "syn", syn), suivi);
        var d = def("---\nid: s\narchitecture: superviseur\nagents:\n  planificateur: plan\n  travailleurs:\n    A: w\n    B: w\n  synthese: syn\n---\n");
        assertThat(o.valider(d)).isEmpty();
        o.executer("run-s", d, Map.of("demande", "go"));
        assertThat(suivi.evenements("run-s").orElseThrow()).extracting(Evenement::id).contains("plan", "t1", "t2", "synthese", "fin");
        assertThat(o.apercu(d)).extracting(Evenement::id).contains("plan", "t-A", "t-B", "synthese");
    }

    @Test
    void plafondDAppelsDOutils() {
        var suivi = new SuiviExecution();
        suivi.demarrer("run-p", "t");
        AtomicInteger reels = new AtomicInteger();
        var outil = new org.springframework.ai.tool.ToolCallback() {
            public org.springframework.ai.tool.definition.ToolDefinition getToolDefinition() {
                return org.springframework.ai.tool.definition.ToolDefinition.builder().name("x").description("d").inputSchema("{}").build();
            }
            public String call(String entree) { reels.incrementAndGet(); return "ok"; }
        };
        var enveloppe = fr.m2ipm.clinique.agent.SuiviOutils.envelopper(outil)[0];
        try (var p = fr.m2ipm.clinique.agent.SuiviOutils.ouvrir(suivi, "run-p", "n")) {
            for (int i = 0; i < 20; i++) { enveloppe.call("{}"); }
        }
        assertThat(reels.get()).isEqualTo(fr.m2ipm.clinique.agent.SuiviOutils.MAX_APPELS_OUTILS);
    }
}
