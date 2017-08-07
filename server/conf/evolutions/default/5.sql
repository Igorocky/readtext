# --- !Ups

create table "TOPIC_HISTORY_RECORDS" (
  "topicId" BIGINT NOT NULL,
  "easiness" INTEGER NOT NULL,
  "score" INTEGER NOT NULL,
  "time" TIMESTAMP NOT NULL
);

alter table "TOPIC_HISTORY_RECORDS" add constraint "TOPIC_HIST_REC_PK" primary key("topicId","time");

alter table "TOPIC_HISTORY_RECORDS" add constraint "TOPIC_HIST_REC_2_TOPIC_FK" foreign key("topicId") references "TOPICS"("id") on update RESTRICT on delete CASCADE;

create table "TOPIC_LEARN_STATE" (
  "topicId" BIGINT NOT NULL PRIMARY KEY,
  "easiness" INTEGER NOT NULL,
  "score" INTEGER NOT NULL,
  "time" TIMESTAMP NOT NULL
);

alter table "TOPIC_LEARN_STATE" add constraint "TOPIC_STATE_2_TOPIC_FK" foreign key("topicId") references "TOPICS"("id") on update RESTRICT on delete CASCADE;



# --- !Downs

alter table "TOPIC_HISTORY_RECORDS" drop constraint "TOPIC_HIST_REC_2_TOPIC_FK";

alter table "TOPIC_HISTORY_RECORDS" drop constraint "TOPIC_HIST_REC_PK";

drop table "TOPIC_HISTORY_RECORDS";

alter table "TOPIC_LEARN_STATE" drop constraint "TOPIC_STATE_2_TOPIC_FK";

drop table "TOPIC_LEARN_STATE";