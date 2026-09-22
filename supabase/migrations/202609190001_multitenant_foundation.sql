-- Multi-tenant foundation for RecepControl DEV.
create table if not exists public.complexes (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  address text,
  city text,
  phone text,
  logo text,
  created_at timestamptz not null default now()
);

create table if not exists public.units (
  id uuid primary key default gen_random_uuid(),
  complex_id uuid not null references public.complexes(id) on delete cascade,
  unit_number text not null,
  building text,
  type text,
  created_at timestamptz not null default now(),
  unique (complex_id, unit_number)
);

alter table public.profiles add column if not exists complex_id uuid references public.complexes(id);
alter table public.profiles add column if not exists unit_id uuid references public.units(id);
alter table public.receipts add column if not exists complex_id uuid references public.complexes(id);
alter table public.receipts add column if not exists unit_id uuid references public.units(id);
alter table public.receipts add column if not exists resident_id uuid references public.profiles(id);
alter table public.announcements add column if not exists complex_id uuid references public.complexes(id);
alter table public.notifications add column if not exists complex_id uuid references public.complexes(id);

create table if not exists public.device_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  token text not null,
  platform text not null default 'ANDROID',
  active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (user_id, token)
);

create index if not exists profiles_complex_idx on public.profiles(complex_id);
create index if not exists profiles_unit_idx on public.profiles(unit_id);
create index if not exists receipts_complex_idx on public.receipts(complex_id);
create index if not exists receipts_resident_idx on public.receipts(resident_id);
create index if not exists device_tokens_user_idx on public.device_tokens(user_id) where active;

alter table public.complexes enable row level security;
alter table public.units enable row level security;
alter table public.device_tokens enable row level security;

create or replace function public.current_user_complex_id()
returns uuid language sql stable security definer set search_path = public
as $$ select complex_id from public.profiles where id = auth.uid(); $$;

create or replace function public.notify_resident_for_receipt()
returns trigger language plpgsql security definer set search_path = public
as $$
begin
  if new.resident_id is not null then
    insert into public.notifications (recipient_id, complex_id, title, message, receipt_id)
    values (new.resident_id, new.complex_id, 'Nuevo paquete en portería',
      'Se registró ' || new.service_category || ' para tu unidad.', new.id);
  end if;
  return new;
end;
$$;

drop trigger if exists receipts_notify_residents on public.receipts;
create trigger receipts_notify_residents after insert on public.receipts
for each row execute function public.notify_resident_for_receipt();

create policy complexes_select_member on public.complexes for select to authenticated
using (id = public.current_user_complex_id());
create policy units_select_member on public.units for select to authenticated
using (complex_id = public.current_user_complex_id());
create policy device_tokens_self on public.device_tokens for all to authenticated
using (user_id = auth.uid()) with check (user_id = auth.uid());

drop policy if exists profiles_select_self_or_vigilante on public.profiles;
create policy profiles_select_same_complex on public.profiles for select to authenticated
using (id = auth.uid() or complex_id = public.current_user_complex_id());

drop policy if exists receipts_select_by_role on public.receipts;
create policy receipts_select_same_complex on public.receipts for select to authenticated
using (complex_id = public.current_user_complex_id() and
  (public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE') or resident_id = auth.uid()));

drop policy if exists receipts_insert_vigilante on public.receipts;
create policy receipts_insert_staff on public.receipts for insert to authenticated
with check (complex_id = public.current_user_complex_id() and
  public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE') and received_by = auth.uid());

grant select on public.complexes, public.units, public.device_tokens to authenticated;
grant insert, update, delete on public.device_tokens to authenticated;
grant execute on function public.current_user_complex_id() to authenticated;
