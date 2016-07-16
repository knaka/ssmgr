# header schema

# --- !Ups

CREATE TABLE body (
  sheet_id int NOT NULL,
  row_idx int,
  col_idx int,
  value text,
  FOREIGN KEY (sheet_id) REFERENCES sheet(id)
);

# --- !Downs

DROP TABLE body;

