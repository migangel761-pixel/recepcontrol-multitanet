-- Verificación de estructura y seguridad para el proyecto RecepControlDemo.
-- Ejecutar en el SQL Editor después de aplicar las cuatro migraciones.

select
  tablename,
  rowsecurity as rls_habilitado
from pg_tables
where schemaname = 'public'
  and tablename in ('profiles', 'receipts', 'announcements', 'notifications')
order by tablename;

select
  schemaname,
  tablename,
  policyname,
  roles,
  cmd
from pg_policies
where schemaname = 'public'
  and tablename in ('profiles', 'receipts', 'announcements', 'notifications')
order by tablename, policyname;

select
  lower(email) as email,
  role,
  apartment
from public.profiles
where lower(email) in (
  lower('carlos.mendoza@recepcontrol.com'),
  lower('maria.gomez@recepcontrol.com')
)
order by email;

select
  reference_code,
  service_category,
  apartment,
  status
from public.receipts
where reference_code like 'DEMO-%'
order by reference_code;
