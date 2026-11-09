package com.exemplo.biblioteca;

import com.exemplo.biblioteca.cli.Menu;
import com.exemplo.biblioteca.db.Banco;
import com.exemplo.biblioteca.service.BibliotecaService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

public class Main {
    public static void main(String[] args) throws IOException {
        Path pasta = Path.of(args.length > 0 ? args[0] : "dados");
        Files.createDirectories(pasta);

        Banco banco = Banco.emArquivo(pasta.resolve("biblioteca"));
        new Menu(new BibliotecaService(banco, Clock.systemDefaultZone())).executar();
    }
}
