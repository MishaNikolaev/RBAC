create table if not exists app_users (
    id bigserial primary key,
    username varchar(100) not null,
    password_hash varchar(255) not null,
    roles varchar(500) not null,
    created_at timestamptz not null default now()
);

create unique index if not exists ux_app_users_username on app_users (username);

