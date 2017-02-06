# --- !Ups

CREATE TABLE "PARAGRAPHS" (
  "id"       BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "checked"  BOOLEAN                                                 NOT NULL,
  "name"     VARCHAR                                                 NOT NULL,
  "expanded" BOOLEAN                                                 NOT NULL,
  "order"    INTEGER                                                 NOT NULL
);

create table "TOPICS" (
  "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,
  "paragraphId" BIGINT NOT NULL,
  "checked" BOOLEAN NOT NULL,
  "title" VARCHAR NOT NULL,
  "order" INTEGER NOT NULL,
  "images" VARCHAR NOT NULL
);

alter table "TOPICS" add constraint "PARAGRAPH_FK" foreign key("paragraphId") references "PARAGRAPHS"("id") on update RESTRICT on delete CASCADE;

# --- !Downs

alter table "TOPICS" drop constraint "PARAGRAPH_FK";
DROP TABLE "TOPICS";
DROP TABLE "PARAGRAPHS";
