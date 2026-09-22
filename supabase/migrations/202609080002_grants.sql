-- Least-privilege Data API grants for the authenticated mobile client.
grant select on public.profiles to authenticated;
grant select, insert, update on public.receipts to authenticated;
grant select, insert on public.announcements to authenticated;
grant select, update on public.notifications to authenticated;
grant usage, select on all sequences in schema public to authenticated;
grant execute on function public.current_user_role() to authenticated;
grant execute on function public.current_user_apartment() to authenticated;
