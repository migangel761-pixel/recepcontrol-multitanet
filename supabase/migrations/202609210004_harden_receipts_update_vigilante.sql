-- Restrict receipt updates to the delivery workflow only.
-- This migration intentionally does not change receipts INSERT or Storage policies.

revoke update on public.receipts from authenticated;
grant update (status, delivered_at) on public.receipts to authenticated;

drop policy if exists receipts_update_vigilante on public.receipts;

create policy receipts_update_vigilante
on public.receipts
for update
to authenticated
using (
  receipts.complex_id = public.current_user_complex_id()
  and public.current_user_role() = 'VIGILANTE'
)
with check (
  receipts.complex_id = public.current_user_complex_id()
  and public.current_user_role() = 'VIGILANTE'
);
