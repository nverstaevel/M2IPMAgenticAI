package fr.m2ipm.clinique.agent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

/** Mesure de reproductibilité : N exécutions du même scénario à une température donnée. Réservé au personnel. */
@RestController
@RequestMapping("/api/agent/mesures")
public class MesuresController {

    public record MesureRequest(@NotBlank String demande, @Min(1) @Max(50) int executions,
                                @DecimalMin("0.0") @DecimalMax("2.0") Double temperature, @Min(1) @Max(20) Integer maxEtapes) {}

    private final AgentReactService agent;

    public MesuresController(AgentReactService agent) { this.agent = agent; }

    @PostMapping
    public Reproductibilite.Synthese mesurer(@Valid @RequestBody MesureRequest r) {
        var resultats = new ArrayList<ResultatAgent>();
        for (int i = 0; i < r.executions(); i++) {
            resultats.add(agent.executer(UUID.randomUUID().toString(), r.demande(), r.maxEtapes() == null ? 8 : r.maxEtapes(), r.temperature()));
        }
        return Reproductibilite.synthese(resultats);
    }
}
