# header schema

# --- !Ups

alter table header drop readable_name;
alter table header add columns text;

# --- !Downs

alter table header drop columns text;
alter table header add readable_name text;

