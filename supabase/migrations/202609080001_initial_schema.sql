-- RecepControl initial Supabase schema
-- Safe to re-run through the Supabase migration API.

create extension if not exists pgcrypto;

do $$
begin
  if not exists (select 1 from pg_type where typname = 'user_role') then
    create type public.user_role as enum ('RESIDENT', 'VIGILANTE');
  end if;
  if not exists (select 1 from pg_type where typname = 'receipt_status') then
    create type public.receipt_status as enum ('EN_PORTERIA', 'ENTREGADO');
  end if;
end
$$;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  email text not null default '',
  phone text,
  role public.user_role not null default 'RESIDENT',
  apartment text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.receipts (
  id uuid primary key default gen_random_uuid(),
  service_category text not null,
  apartment text not null,
  status public.receipt_status not null default 'EN_PORTERIA',
  reference_code text not null unique,
  received_by uuid not null references public.profiles(id),
  received_at timestamptz not null default now(),
  delivered_at timestamptz,
  guard_note text not null default '',
  photo_path text,
  created_at timestamptz not null default now(),
  constraint delivered_receipt_has_date check (
    status = 'EN_PORTERIA' or delivered_at is not null
  )
);

create table if not exists public.announcements (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  content text not null,
  author_id uuid not null references public.profiles(id),
  created_at timestamptz not null default now()
);

create table if not exists public.notifications (
  id uuid primary key default gen_random_uuid(),
  recipient_id uuid not null references public.profiles(id) on delete cascade,
  title text not null,
  message text not null,
  receipt_id uuid references public.receipts(id) on delete cascade,
  read_at timestamptz,
  created_at timestamptz not null default now()
);

create index if not exists receipts_apartment_idx on public.receipts (apartment);
create index if not exists receipts_status_idx on public.receipts (status);
create index if not exists notifications_recipient_idx on public.notifications (recipient_id, created_at desc);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
security invoker
set search_path = public
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, email, phone, role, apartment)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', ''),
    coalesce(new.email, ''),
    new.raw_user_meta_data ->> 'phone',
    case
      when upper(coalesce(new.raw_user_meta_data ->> 'role', 'RESIDENT')) = 'VIGILANTE'
        then 'VIGILANTE'::public.user_role
      else 'RESIDENT'::public.user_role
    end,
    new.raw_user_meta_data ->> 'apartment'
  );
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

create or replace function public.current_user_role()
returns public.user_role
language sql
stable
security definer
set search_path = public
as $$
  select role from public.profiles where id = auth.uid();
$$;

create or replace function public.current_user_apartment()
returns text
language sql
stable
security definer
set search_path = public
as $$
  select apartment from public.profiles where id = auth.uid();
$$;

create or replace function public.notify_residents_for_receipt()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.notifications (recipient_id, title, message, receipt_id)
  select
    p.id,
    'Nuevo recibo o evento en portería',
    'Se registró ' || new.service_category || ' para ' || new.apartment || '.',
    new.id
  from public.profiles p
  where p.role = 'RESIDENT'
    and (
      p.apartment = new.apartment
      or new.apartment ilike '%Zonas Comunes%'
      or new.apartment ilike '%Edificio%'
    );
  return new;
end;
$$;

drop trigger if exists receipts_notify_residents on public.receipts;
create trigger receipts_notify_residents
after insert on public.receipts
for each row execute function public.notify_residents_for_receipt();

alter table public.profiles enable row level security;
alter table public.receipts enable row level security;
alter table public.announcements enable row level security;
alter table public.notifications enable row level security;

drop policy if exists profiles_select_self_or_vigilante on public.profiles;
create policy profiles_select_self_or_vigilante
on public.profiles for select to authenticated
using (id = auth.uid() or public.current_user_role() = 'VIGILANTE');

drop policy if exists profiles_update_self on public.profiles;
create policy profiles_update_self
on public.profiles for update to authenticated
using (id = auth.uid())
with check (id = auth.uid());

drop policy if exists receipts_select_by_role on public.receipts;
create policy receipts_select_by_role
on public.receipts for select to authenticated
using (
  public.current_user_role() = 'VIGILANTE'
  or apartment = public.current_user_apartment()
  or apartment ilike '%Zonas Comunes%'
  or apartment ilike '%Edificio%'
);

drop policy if exists receipts_insert_vigilante on public.receipts;
create policy receipts_insert_vigilante
on public.receipts for insert to authenticated
with check (
  public.current_user_role() = 'VIGILANTE'
  and received_by = auth.uid()
);

drop policy if exists receipts_update_vigilante on public.receipts;
create policy receipts_update_vigilante
on public.receipts for update to authenticated
using (public.current_user_role() = 'VIGILANTE')
with check (public.current_user_role() = 'VIGILANTE');

drop policy if exists announcements_select_authenticated on public.announcements;
create policy announcements_select_authenticated
on public.announcements for select to authenticated
using (true);

drop policy if exists announcements_insert_vigilante on public.announcements;
create policy announcements_insert_vigilante
on public.announcements for insert to authenticated
with check (public.current_user_role() = 'VIGILANTE' and author_id = auth.uid());

drop policy if exists notifications_select_recipient on public.notifications;
create policy notifications_select_recipient
on public.notifications for select to authenticated
using (recipient_id = auth.uid());

drop policy if exists notifications_update_recipient on public.notifications;
create policy notifications_update_recipient
on public.notifications for update to authenticated
using (recipient_id = auth.uid())
with check (recipient_id = auth.uid());

insert into storage.buckets (id, name, public)
values ('receipt-photos', 'receipt-photos', false)
on conflict (id) do update set public = false;

drop policy if exists receipt_photos_insert_vigilante on storage.objects;
create policy receipt_photos_insert_vigilante
on storage.objects for insert to authenticated
with check (
  bucket_id = 'receipt-photos'
  and public.current_user_role() = 'VIGILANTE'
);

drop policy if exists receipt_photos_select_by_role on storage.objects;
create policy receipt_photos_select_by_role
on storage.objects for select to authenticated
using (
  bucket_id = 'receipt-photos'
  and (
    public.current_user_role() = 'VIGILANTE'
    or exists (
      select 1
      from public.receipts r
      where r.photo_path = name
        and (
          r.apartment = public.current_user_apartment()
          or r.apartment ilike '%Zonas Comunes%'
          or r.apartment ilike '%Edificio%'
        )
    )
  )
);
