-- Datos de demostración para un proyecto Supabase separado.
-- Crear y confirmar primero los usuarios en Authentication.
-- Este script no crea usuarios ni contiene contraseñas.

do $$
declare
  vigilante_id uuid;
  residente_id uuid;
begin
  select id into vigilante_id
  from public.profiles
  where lower(email) = lower('carlos.mendoza@recepcontrol.com')
  limit 1;

  select id into residente_id
  from public.profiles
  where lower(email) = lower('maria.gomez@recepcontrol.com')
  limit 1;

  if vigilante_id is null or residente_id is null then
    raise exception 'Crea y confirma primero los dos usuarios de demo en Authentication.';
  end if;

  update public.profiles
  set role = 'VIGILANTE',
      full_name = coalesce(nullif(full_name, ''), 'Carlos Mendoza'),
      phone = coalesce(phone, '3104567890'),
      apartment = coalesce(apartment, 'Administración / Consejo')
  where id = vigilante_id;

  update public.profiles
  set role = 'RESIDENT',
      full_name = coalesce(nullif(full_name, ''), 'María Gómez'),
      phone = coalesce(phone, '3201234567'),
      apartment = coalesce(apartment, 'Torre 1 - 302')
  where id = residente_id;

  insert into public.receipts (
    service_category,
    apartment,
    status,
    reference_code,
    received_by,
    guard_note
  )
  select
    'Cuota de Administración Mensual',
    'Torre 1 - 302',
    'EN_PORTERIA',
    'DEMO-0001',
    vigilante_id,
    'Registro de demostración para el apartamento Torre 1 - 302.'
  where not exists (
    select 1 from public.receipts where reference_code = 'DEMO-0001'
  );

  insert into public.receipts (
    service_category,
    apartment,
    status,
    reference_code,
    received_by,
    delivered_at,
    guard_note
  )
  select
    'Paquete o Encomienda en Portería',
    'Torre 1 - 302',
    'ENTREGADO',
    'DEMO-0002',
    vigilante_id,
    now() - interval '1 day',
    'Registro entregado de demostración.'
  where not exists (
    select 1 from public.receipts where reference_code = 'DEMO-0002'
  );

  insert into public.announcements (title, content, author_id)
  select
    'Mantenimiento de zonas comunes',
    'El sábado se realizará mantenimiento preventivo en las zonas comunes.',
    vigilante_id
  where not exists (
    select 1
    from public.announcements
    where title = 'Mantenimiento de zonas comunes'
  );
end $$;
