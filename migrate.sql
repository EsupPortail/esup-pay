ALTER TABLE paybox_evt RENAME TO pay_evt;
ALTER TABLE paybox_evt_montant RENAME TO pay_evt_montant;
ALTER TABLE paybox_evt_resp_logins RENAME TO pay_evt_resp_logins;
ALTER TABLE paybox_evt_viewer_logins RENAME TO pay_evt_viewer_logins;
ALTER TABLE paybox_transaction_log RENAME TO pay_transaction_log;

ALTER TABLE pay_evt_resp_logins RENAME COLUMN paybox_evt TO pay_evt;
ALTER TABLE pay_evt_viewer_logins RENAME COLUMN paybox_evt TO pay_evt;
ALTER TABLE email_fields_map_reference RENAME COLUMN paybox_evt_montant TO pay_evt_montant;
ALTER TABLE pay_transaction_log RENAME COLUMN paybox_evt_montant TO pay_evt_montant;



do $$
declare r record;
begin
for r in select binary_file from big_file where binary_file is not null  loop
execute 'ALTER LARGE OBJECT ' || r.binary_file || ' OWNER TO esuppay';
end loop;
end$$;


esuppay=# do $$
declare r record;
begin
for r in select * from big_file where binary_file is not null loop
execute 'GRANT SELECT,UPDATE ON LARGE OBJECT ' || r.binary_file || ' TO esuppay';
end loop;
end$$;
DO


Bash ... : 
for tbl in `psql -qAt -c "select tablename from pg_tables where schemaname = 'public';" esuppay` ; do  psql -c "alter table \"$tbl\" owner to esuppay" esuppay ; done
for tbl in `psql -qAt -c "select sequence_name from information_schema.sequences where sequence_schema = 'public';" esuppay` ; do  psql -c "alter table \"$tbl\" owner to esuppay" esuppay ; done
