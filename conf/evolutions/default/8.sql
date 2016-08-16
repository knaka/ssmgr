# header schema

# --- !Ups

alter table body drop col_idx;
alter table body drop value;
alter table body add columns text;

# --- !Downs

alter table body drop columns;
alter table body add value text;
alter table body add col_idx integer;
