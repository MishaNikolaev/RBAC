create table if not exists passengers (
    id bigserial primary key,
    name varchar(200) not null,
    email varchar(320) not null,
    phone varchar(50),
    created_at timestamptz not null default now()
);

create unique index if not exists ux_passengers_email on passengers (email);

create table if not exists drivers (
    id bigserial primary key,
    name varchar(200) not null,
    email varchar(320) not null,
    phone varchar(50),
    license_number varchar(100) not null,
    status varchar(30) not null,
    created_at timestamptz not null default now()
);

create unique index if not exists ux_drivers_email on drivers (email);
create index if not exists ix_drivers_status on drivers (status);

create table if not exists trips (
    id bigserial primary key,
    passenger_id bigint not null references passengers(id),
    driver_id bigint references drivers(id),
    status varchar(30) not null,
    origin varchar(500) not null,
    destination varchar(500) not null,
    price numeric(12,2),
    rating int,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists ix_trips_status on trips (status);
create index if not exists ix_trips_passenger_id on trips (passenger_id);

create table if not exists notification_tasks (
    id bigserial primary key,
    trip_id bigint not null references trips(id),
    recipient_type varchar(30) not null,
    recipient_id bigint not null,
    message varchar(2000) not null,
    status varchar(30) not null,
    attempts int not null default 0,
    created_at timestamptz not null default now()
);

create index if not exists ix_notification_tasks_status on notification_tasks (status);
