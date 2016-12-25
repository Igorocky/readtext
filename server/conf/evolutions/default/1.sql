# --- !Ups

create table "TEXTS" ("title" VARCHAR NOT NULL PRIMARY KEY,"content" VARCHAR NOT NULL);

# --- !Downs

DROP TABLE "TEXTS";
