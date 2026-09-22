grant select, update on public.complexes to authenticated;
grant select, insert, update, delete on public.units to authenticated;
grant update (full_name, email, phone) on public.profiles to authenticated;
grant insert, update on public.announcements to authenticated;

create policy complexes_admin_update on public.complexes for update to authenticated
using (id = public.current_user_complex_id() and public.current_user_role() = 'ADMINISTRADOR')
with check (id = public.current_user_complex_id());

create policy units_admin_insert on public.units for insert to authenticated
with check (complex_id = public.current_user_complex_id() and public.current_user_role() = 'ADMINISTRADOR');
create policy units_admin_update on public.units for update to authenticated
using (complex_id = public.current_user_complex_id() and public.current_user_role() = 'ADMINISTRADOR')
with check (complex_id = public.current_user_complex_id());
create policy units_admin_delete on public.units for delete to authenticated
using (complex_id = public.current_user_complex_id() and public.current_user_role() = 'ADMINISTRADOR');

create policy profiles_admin_update_same_complex on public.profiles for update to authenticated
using (complex_id = public.current_user_complex_id() and public.current_user_role() = 'ADMINISTRADOR')
with check (complex_id = public.current_user_complex_id());

drop policy if exists announcements_select_authenticated on public.announcements;
create policy announcements_select_same_complex on public.announcements for select to authenticated
using (complex_id = public.current_user_complex_id());
drop policy if exists announcements_insert_vigilante on public.announcements;
create policy announcements_insert_staff on public.announcements for insert to authenticated
with check (complex_id = public.current_user_complex_id() and public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE') and author_id = auth.uid());
create policy announcements_update_admin on public.announcements for update to authenticated
using (complex_id = public.current_user_complex_id() and public.current_user_role() = 'ADMINISTRADOR')
with check (complex_id = public.current_user_complex_id());
