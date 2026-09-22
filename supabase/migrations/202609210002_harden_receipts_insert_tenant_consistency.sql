-- Harden receipts INSERT tenant and relationship consistency.
-- This migration intentionally does not change receipts UPDATE or Storage policies.

drop policy if exists receipts_insert_staff on public.receipts;

create policy receipts_insert_staff
on public.receipts
for insert
to authenticated
with check (
  complex_id = public.current_user_complex_id()
  and received_by = auth.uid()
  and public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE')
  and unit_id is not null
  and exists (
    select 1
    from public.units u
    where u.id = unit_id
      and u.complex_id = complex_id
  )
  and (
    resident_id is null
    or exists (
      select 1
      from public.profiles resident_profile
      where resident_profile.id = resident_id
        and resident_profile.role = 'RESIDENT'
        and resident_profile.complex_id = complex_id
        and resident_profile.unit_id = unit_id
    )
  )
);
