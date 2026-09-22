-- Restrict receipt photo reads to receipts visible within the caller's complex.
-- This migration intentionally does not change Storage INSERT or receipt policies.

drop policy if exists receipt_photos_select_by_role on storage.objects;

create policy receipt_photos_select_by_role
on storage.objects
for select
to authenticated
using (
  storage.objects.bucket_id = 'receipt-photos'
  and exists (
    select 1
    from public.receipts r
    where r.photo_path = storage.objects.name
      and r.complex_id = public.current_user_complex_id()
      and (
        public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE')
        or r.resident_id = auth.uid()
      )
  )
);
