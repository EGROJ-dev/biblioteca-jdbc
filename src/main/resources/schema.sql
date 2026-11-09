CREATE TABLE IF NOT EXISTS livro (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    autor  VARCHAR(100) NOT NULL,
    ano    INT
);

CREATE TABLE IF NOT EXISTS membro (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS emprestimo (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    livro_id        INT  NOT NULL,
    membro_id       INT  NOT NULL,
    data_emprestimo DATE NOT NULL,
    data_devolucao  DATE,
    FOREIGN KEY (livro_id)  REFERENCES livro(id),
    FOREIGN KEY (membro_id) REFERENCES membro(id)
);
