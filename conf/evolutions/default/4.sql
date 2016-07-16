# header schema

# --- !Ups


CREATE TABLE header (
	sheet_id int NOT NULL,
	col_idx int,
	readable_name text,
  FOREIGN KEY (sheet_id) REFERENCES sheet(id)
);

# --- !Downs

DROP TABLE header;
