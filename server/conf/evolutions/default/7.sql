# --- !Ups

truncate table "TOPIC_HISTORY_RECORDS";

truncate table "TOPIC_LEARN_STATE";

alter table "TOPIC_HISTORY_RECORDS" drop column if exists "easiness";
alter table "TOPIC_HISTORY_RECORDS" add column if not exists "activationTime" TIMESTAMP NOT NULL;
alter table "TOPIC_HISTORY_RECORDS" add column if not exists "comment" VARCHAR;

alter table "TOPIC_LEARN_STATE" drop column if exists "easiness";
alter table "TOPIC_LEARN_STATE" add column if not exists "activationTime" TIMESTAMP NOT NULL;
alter table "TOPIC_LEARN_STATE" add column if not exists "comment" VARCHAR;

# --- !Downs


