revoke execute on function public.handle_new_user() from anon, authenticated;
revoke execute on function public.notify_residents_for_receipt() from anon, authenticated;
revoke execute on function public.notify_resident_for_receipt() from anon, authenticated;
revoke execute on function public.set_updated_at() from anon, authenticated;
revoke execute on function public.rls_auto_enable() from anon, authenticated;
revoke execute on function public.current_user_role() from anon;
revoke execute on function public.current_user_apartment() from anon;
revoke execute on function public.current_user_complex_id() from anon;

drop policy if exists receipt_photos_insert_vigilante on storage.objects;
create policy receipt_photos_insert_vigilante on storage.objects for insert to authenticated
with check (bucket_id = 'receipt-photos' and public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE'));

drop policy if exists receipt_photos_select_by_role on storage.objects;
create policy receipt_photos_select_by_role on storage.objects for select to authenticated
using (bucket_id = 'receipt-photos' and (
  public.current_user_role() in ('ADMINISTRADOR', 'VIGILANTE') or
  exists (select 1 from public.receipts r where r.photo_path = name and r.resident_id = auth.uid())
));
