# --- !Ups

alter table "TOPICS" ADD COLUMN "tags" VARCHAR DEFAULT '' NOT NULL;

# --- !Downs

alter table "TOPICS" drop COLUMN "tags";

