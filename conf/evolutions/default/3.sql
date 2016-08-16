# Users schema

# --- !Ups

CREATE TABLE sheet (
  id SERIAL,
  name text,
  readable_name text,
  col_max int,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE sheet;
