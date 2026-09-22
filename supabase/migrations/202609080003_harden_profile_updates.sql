-- Residents must not change their role or apartment from the client.
revoke update on public.profiles from authenticated;
grant update (full_name, email, phone) on public.profiles to authenticated;

drop policy if exists profiles_update_self on public.profiles;
create policy profiles_update_self
on public.profiles for update to authenticated
using (id = auth.uid())
with check (id = auth.uid());
