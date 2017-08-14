# --- !Ups

alter table "TOPIC_HISTORY_RECORDS" alter column "score" BIGINT NOT NULL;

alter table "TOPIC_LEARN_STATE" alter column "score" BIGINT NOT NULL;

# --- !Downs


