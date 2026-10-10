package com.gui.financas.ai;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Transforma um arquivo de áudio em texto usando o TranscriptionModel do Spring AI. */
@Service
public class TranscricaoService {

    private final TranscriptionModel transcriptionModel;

    public TranscricaoService(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcrever(MultipartFile arquivo) throws IOException {
        // O provedor identifica o formato pela extensão, então o nome do arquivo precisa ir junto.
        String nomeOriginal = arquivo.getOriginalFilename();
        String nome = (nomeOriginal == null || nomeOriginal.isBlank()) ? "audio.mp3" : nomeOriginal;
        Resource audio = new ByteArrayResource(arquivo.getBytes()) {
            @Override
            public String getFilename() {
                return nome;
            }
        };

        String texto;
        try {
            texto = transcriptionModel.call(new AudioTranscriptionPrompt(audio)).getResult().getOutput();
        } catch (RuntimeException e) {
            throw new FalhaProvedorIaException(
                    "Falha ao transcrever o áudio. Verifique a variável OPENAI_API_KEY e o formato do arquivo.", e);
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Não consegui entender o áudio. Tente gravar novamente.");
        }
        return texto.trim();
    }
}
