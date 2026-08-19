create table if not exists dummy
(
    id varchar
        constraint dummy_pk primary key
);

insert into dummy (id)
select 'dummy-table-id-1'
where not exists (select 1 from dummy where id = 'dummy-table-id-1');