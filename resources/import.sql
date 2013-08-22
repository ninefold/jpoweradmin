-- SQL statements which are executed at application startup if hibernate.hbm2ddl.auto is 'create' or 'create-drop'
--CREATE UNIQUE INDEX user_name_idx ON `user` (userName);
--CREATE UNIQUE INDEX name_index ON domains(name);
--CREATE INDEX rec_name_index ON records(name);
CREATE INDEX nametype_index ON records(name,`type`);



