alter table trips
    add column if not exists distance_km numeric(10,2) not null default 0;

