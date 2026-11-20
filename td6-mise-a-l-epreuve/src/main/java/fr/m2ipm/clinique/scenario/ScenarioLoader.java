package fr.m2ipm.clinique.scenario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * Lit les scénarios : les fichiers .md du dossier {@code scenarios.dir} (par défaut ./scenarios, relu à chaque appel :
 * on modifie un fichier, on actualise la page) puis ceux fournis dans les ressources (classpath:scenarios/).
 * Un fichier du dossier remplace celui des ressources qui a le même id. Les fichiers dont le nom commence par « _ » sont ignorés.
 */
@Component
public class ScenarioLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioLoader.class);

    private final Path dossier;

    public ScenarioLoader(@Value("${scenarios.dir:scenarios}") String dossier) {
        this.dossier = Path.of(dossier);
    }

    public List<ScenarioDef> charger() {
        Map<String, ScenarioDef> parId = new LinkedHashMap<>();
        try {
            for (Resource r : new PathMatchingResourcePatternResolver().getResources("classpath*:scenarios/*.md")) {
                String nom = r.getFilename();
                if (nom == null || nom.startsWith("_")) { continue; }
                try {
                    ajouter(parId, lire(new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8), "(fourni) " + nom, nom));
                } catch (IOException e) {
                    LOG.warn("Scénario {} illisible : {}", nom, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.debug("Pas de scénario dans les ressources : {}", e.getMessage());
        }
        if (Files.isDirectory(dossier)) {
            try (Stream<Path> fichiers = Files.list(dossier)) {
                for (Path f : fichiers.filter(p -> p.getFileName().toString().endsWith(".md")).sorted().toList()) {
                    String nom = f.getFileName().toString();
                    if (nom.startsWith("_")) { continue; }
                    try {
                        ajouter(parId, lire(Files.readString(f, StandardCharsets.UTF_8), dossier + "/" + nom, nom));
                    } catch (IOException e) {
                        LOG.warn("Scénario {} illisible : {}", f, e.getMessage());
                    }
                }
            } catch (IOException e) {
                LOG.warn("Dossier de scénarios illisible : {}", e.getMessage());
            }
        }
        return new ArrayList<>(parId.values());
    }

    private static void ajouter(Map<String, ScenarioDef> parId, ScenarioDef d) {
        parId.put(d.id(), d);
    }

    /** Analyse un fichier : en-tête YAML entre deux lignes « --- », puis le texte Markdown. Ne lève jamais : les problèmes deviennent des erreurs du scénario. */
    @SuppressWarnings("unchecked")
    static ScenarioDef lire(String contenu, String fichier, String nomFichier) {
        String idDefaut = nomFichier.replaceAll("\\.md$", "").toLowerCase().replaceAll("[^a-z0-9]+", "-");
        List<String> erreurs = new ArrayList<>();
        String[] lignes = contenu.replace("\r\n", "\n").split("\n", -1);
        int debut = 0;
        while (debut < lignes.length && lignes[debut].isBlank()) { debut++; }
        if (debut >= lignes.length || !lignes[debut].trim().equals("---")) {
            erreurs.add("Le fichier doit commencer par un en-tête YAML entre deux lignes « --- ».");
            return new ScenarioDef(idDefaut, idDefaut, "", List.of(), Map.of(), Map.of(), contenu, fichier, erreurs);
        }
        int fin = -1;
        for (int i = debut + 1; i < lignes.length; i++) {
            if (lignes[i].trim().equals("---")) { fin = i; break; }
        }
        if (fin < 0) {
            erreurs.add("L'en-tête YAML n'est pas fermé par une ligne « --- ».");
            return new ScenarioDef(idDefaut, idDefaut, "", List.of(), Map.of(), Map.of(), contenu, fichier, erreurs);
        }
        String yaml = String.join("\n", List.of(lignes).subList(debut + 1, fin));
        String corps = String.join("\n", List.of(lignes).subList(fin + 1, lignes.length)).strip();
        Map<String, Object> m;
        try {
            Object o = new Yaml().load(yaml);
            if (!(o instanceof Map)) {
                erreurs.add("L'en-tête YAML doit être une suite de « clé: valeur ».");
                return new ScenarioDef(idDefaut, idDefaut, "", List.of(), Map.of(), Map.of(), corps, fichier, erreurs);
            }
            m = (Map<String, Object>) o;
        } catch (RuntimeException e) {
            erreurs.add("YAML invalide : " + premiereLigne(e.getMessage()));
            return new ScenarioDef(idDefaut, idDefaut, "", List.of(), Map.of(), Map.of(), corps, fichier, erreurs);
        }
        String id = texte(m.get("id"), idDefaut);
        if (!id.matches("[a-z0-9][a-z0-9-]{0,63}")) {
            erreurs.add("id « " + id + " » invalide : minuscules, chiffres et tirets seulement.");
        }
        String titre = texte(m.get("titre"), id);
        String architecture = texte(m.get("architecture"), "");
        Map<String, Object> agents = m.get("agents") instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of();
        Map<String, Object> options = m.get("options") instanceof Map<?, ?> o ? (Map<String, Object>) o : Map.of();
        List<ChampEntree> entree = new ArrayList<>();
        if (m.get("entree") instanceof List<?> l) {
            for (Object c : l) {
                if (c instanceof Map<?, ?> champ) {
                    entree.add(champ((Map<String, Object>) champ, erreurs));
                } else {
                    erreurs.add("Chaque élément de « entree » doit être un bloc avec au moins un « id ».");
                }
            }
        }
        return new ScenarioDef(id, titre, architecture, entree, agents, options, corps, fichier, erreurs);
    }

    @SuppressWarnings("unchecked")
    private static ChampEntree champ(Map<String, Object> c, List<String> erreurs) {
        String id = texte(c.get("id"), "");
        if (id.isBlank()) { erreurs.add("Un champ d'entrée n'a pas d'« id »."); }
        String type = texte(c.get("type"), "texte");
        List<ChampEntree.Option> options = new ArrayList<>();
        if (c.get("options") instanceof List<?> l) {
            for (Object o : l) {
                String s = String.valueOf(o);
                int i = s.indexOf('|');
                options.add(i < 0 ? new ChampEntree.Option(s, s) : new ChampEntree.Option(s.substring(0, i).trim(), s.substring(i + 1).trim()));
            }
        }
        if (type.equals("choix") && options.isEmpty()) { erreurs.add("Le champ « " + id + " » est de type choix sans « options »."); }
        return new ChampEntree(id, texte(c.get("libelle"), id), type, texte(c.get("defaut"), ""), options);
    }

    private static String texte(Object o, String defaut) {
        return o == null ? defaut : String.valueOf(o).trim();
    }

    private static String premiereLigne(String s) {
        return s == null ? "" : s.lines().findFirst().orElse(s);
    }
}
