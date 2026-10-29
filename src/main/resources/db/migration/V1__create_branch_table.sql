create table company (
   id   bigserial primary key,
   name varchar(150) not null
);

create table branch (
   id         bigserial primary key,
   company_id bigint not null,
   name       varchar(100) not null,
   city       varchar(100) not null,
   constraint fk_branch_company foreign key ( company_id )
      references company ( id )
);