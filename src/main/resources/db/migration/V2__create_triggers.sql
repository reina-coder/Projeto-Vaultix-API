-- V2: Triggers de automacao para auditoria e compliance LGPD

-- Trigger 1: ao abrir incidente, registra na auditoria automaticamente
CREATE OR REPLACE TRIGGER TRG_AUDIT_NOVO_INCIDENTE
AFTER INSERT ON INCIDENTE
FOR EACH ROW
DECLARE
    v_tipo_acao VARCHAR2(100);
    v_descricao VARCHAR2(500);
    v_resultado VARCHAR2(200);
BEGIN
    IF :NEW.SEVERIDADE = 'CRITICA' THEN
        v_tipo_acao := 'ESCALONAMENTO_CRITICO';
        v_descricao := 'Incidente critico [' || :NEW.TIPO_INCIDENTE || '] escalado automaticamente. Equipe de resposta notificada.';
        v_resultado := 'EM_ANDAMENTO';
    ELSE
        v_tipo_acao := 'ABERTURA_INCIDENTE';
        v_descricao := 'Incidente [' || :NEW.TIPO_INCIDENTE || '] registrado. Severidade: ' || :NEW.SEVERIDADE || '.';
        v_resultado := 'PENDENTE';
    END IF;

    INSERT INTO AUDITORIA_SEGURANCA (ID_CLIENTE, ID_INCIDENTE, TIPO_ACAO, DESCRICAO, DT_REGISTRO, RESULTADO)
    VALUES (:NEW.ID_CLIENTE, :NEW.ID_INCIDENTE, v_tipo_acao, v_descricao, SYSDATE, v_resultado);
END;
/

-- Trigger 2: incidentes criticos entram direto como EM_ATENDIMENTO
CREATE OR REPLACE TRIGGER TRG_ESCALONA_STATUS_CRITICO
BEFORE INSERT ON INCIDENTE
FOR EACH ROW
WHEN (NEW.SEVERIDADE = 'CRITICA')
BEGIN
    :NEW.STATUS := 'EM_ATENDIMENTO';
END;
/

-- Trigger 3: resolucao de incidente gera registro de encerramento
CREATE OR REPLACE TRIGGER TRG_AUDIT_RESOLUCAO_INCIDENTE
AFTER UPDATE OF STATUS ON INCIDENTE
FOR EACH ROW
WHEN (NEW.STATUS = 'RESOLVIDO' AND OLD.STATUS != 'RESOLVIDO')
BEGIN
    INSERT INTO AUDITORIA_SEGURANCA (ID_CLIENTE, ID_INCIDENTE, TIPO_ACAO, DESCRICAO, DT_REGISTRO, RESULTADO)
    VALUES (
        :NEW.ID_CLIENTE,
        :NEW.ID_INCIDENTE,
        'RESOLUCAO_INCIDENTE',
        'Incidente [' || :NEW.TIPO_INCIDENTE || '] encerrado. Registro gerado para auditoria.',
        SYSDATE,
        'CONCLUIDO'
    );
END;
/

-- Trigger 4: vencimento de assinatura ATIVA -> VENCIDA gera registro de auditoria (LGPD Art. 16)
CREATE OR REPLACE TRIGGER TRG_AUDIT_ASSINATURA_VENCIDA
AFTER UPDATE OF STATUS ON ASSINATURA
FOR EACH ROW
WHEN (NEW.STATUS = 'VENCIDA' AND OLD.STATUS = 'ATIVA')
BEGIN
    INSERT INTO AUDITORIA_SEGURANCA (ID_CLIENTE, ID_INCIDENTE, TIPO_ACAO, DESCRICAO, DT_REGISTRO, RESULTADO)
    VALUES (
        :NEW.ID_CLIENTE,
        NULL,
        'ASSINATURA_VENCIDA',
        'Assinatura ' || :NEW.ID_ASSINATURA || ' venceu em ' || TO_CHAR(:NEW.DT_VENCIMENTO, 'DD/MM/YYYY') ||
        '. Cobertura suspensa. Dados mantidos por 30 dias conforme LGPD Art. 16.',
        SYSDATE,
        'PENDENTE'
    );
END;
/
