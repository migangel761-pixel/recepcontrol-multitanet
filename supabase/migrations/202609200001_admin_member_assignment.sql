create or replace function public.admin_update_member(
  target_id uuid,
  new_role public.user_role,
  new_unit_id uuid default null
)
returns public.profiles
language plpgsql
security definer
set search_path = public
as $$
declare
  caller_complex uuid;
  updated_profile public.profiles;
begin
  select complex_id into caller_complex
  from public.profiles
  where id = auth.uid() and role = 'ADMINISTRADOR';

  if caller_complex is null then
    raise exception 'Solo un administrador asignado a un conjunto puede gestionar usuarios';
  end if;

  if new_unit_id is not null and not exists (
    select 1 from public.units where id = new_unit_id and complex_id = caller_complex
  ) then
    raise exception 'La unidad no pertenece al conjunto del administrador';
  end if;

  update public.profiles
  set role = new_role, unit_id = new_unit_id, updated_at = now()
  where id = target_id and complex_id = caller_complex
  returning * into updated_profile;

  if updated_profile.id is null then
    raise exception 'El usuario no pertenece al conjunto del administrador';
  end if;
  return updated_profile;
end;
$$;

revoke execute on function public.admin_update_member(uuid, public.user_role, uuid) from public, anon;
grant execute on function public.admin_update_member(uuid, public.user_role, uuid) to authenticated;
