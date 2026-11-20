-- Ejecutar después de crear billing_documents y antes de habilitar escrituras.
ALTER TABLE billing_documents
    ALTER COLUMN document_type SET NOT NULL,
    ADD CONSTRAINT ck_billing_document_identity CHECK (
        btrim(document_type) <> ''
        AND (nullif(btrim(document_number), '') IS NOT NULL
             OR nullif(btrim(external_number), '') IS NOT NULL)
    );

CREATE UNIQUE INDEX uq_billing_document_number
    ON billing_documents (btrim(document_type), btrim(document_number))
    WHERE nullif(btrim(document_number), '') IS NOT NULL;

CREATE UNIQUE INDEX uq_billing_external_number
    ON billing_documents (btrim(document_type), btrim(external_number))
    WHERE nullif(btrim(document_number), '') IS NULL;
