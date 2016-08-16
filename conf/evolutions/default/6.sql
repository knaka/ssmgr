# header schema

# --- !Ups

alter table header drop col_idx;

# --- !Downs

alter table header add col_idx int;

