# --- !Ups

alter table "PARAGRAPHS" drop COLUMN IF EXISTS "checked";

alter table "TOPICS" drop COLUMN IF EXISTS "checked";

alter table "PARAGRAPHS" ADD COLUMN "paragraphId" BIGINT;

alter table "PARAGRAPHS"
  add constraint "PAR_PARAGRAPH_FK" foreign key("paragraphId") references "PARAGRAPHS"("id")
  on update RESTRICT
  on delete CASCADE
;



# --- !Downs

alter table "PARAGRAPHS" drop COLUMN "paragraphId";

alter table "PARAGRAPHS" drop constraint "PAR_PARAGRAPH_FK"