-- V4: Tabela de usuarios para autenticacao Spring Security + JWT
-- Senha do admin abaixo eh "admin123" criptografada com BCrypt (cost 10)

CREATE TABLE USUARIO (
    ID_USUARIO  NUMBER         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USERNAME    VARCHAR2(100)  NOT NULL UNIQUE,
    PASSWORD    VARCHAR2(200)  NOT NULL,
    ROLE        VARCHAR2(20)   DEFAULT 'USER' NOT NULL
                CONSTRAINT CK_ROLE CHECK (ROLE IN ('USER','ADMIN'))
);

INSERT INTO USUARIO (USERNAME, PASSWORD, ROLE)
VALUES ('admin', '$2b$10$ZEsD8Wi2FsPOfc4DsFbQnuT/7TbTosoHjlNy1fmTmntx1ZKkyK3NK', 'ADMIN');
