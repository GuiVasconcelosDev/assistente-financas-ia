package com.gui.financas.web;

import com.gui.financas.ai.AssistenteFinanceiro;
import com.gui.financas.ai.TranscricaoService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/assistente")
public class AssistenteController {

    private final AssistenteFinanceiro assistente;
    private final TranscricaoService transcricao;

    public AssistenteController(AssistenteFinanceiro assistente, TranscricaoService transcricao) {
        this.assistente = assistente;
        this.transcricao = transcricao;
    }

    /** Fluxo por texto: mensagem -> IA (com ferramentas) -> resposta. */
    @PostMapping("/texto")
    public AssistenteResponse texto(@Valid @RequestBody MensagemRequest request) {
        return new AssistenteResponse(null, assistente.responder(request.mensagem()));
    }

    /** Fluxo por voz: áudio -> texto -> IA (com ferramentas) -> resposta. */
    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssistenteResponse audio(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo de áudio no campo 'arquivo'.");
        }
        String texto = transcricao.transcrever(arquivo);
        return new AssistenteResponse(texto, assistente.responder(texto));
    }
}
