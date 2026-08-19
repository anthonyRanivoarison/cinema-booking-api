create table if not exists dummy_uuid
(
    id varchar
        constraint dummy_uuid_pk primary key
);

insert into dummy_uuid (id)
select 'dummy-uuid-id-1'
where not exists (select 1 from dummy_uuid where id = 'dummy-uuid-id-1');