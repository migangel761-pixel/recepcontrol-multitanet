-- New accounts are residents by default. Vigilante access must be granted by an administrator.
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
    'RESIDENT'::public.user_role,
    new.raw_user_meta_data ->> 'apartment'
  );
  return new;
end;
$$;
