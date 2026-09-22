import { createClient } from '@supabase/supabase-js'
import { randomUUID } from 'node:crypto'

const url = 'https://ubohhppltdadhxpagblc.supabase.co'
const key = 'sb_publishable_9HGpkUJE6uCuu54YFu6teQ_BkiKHe1Q'
const password = process.env.TEST_PASSWORD
if (!password) throw new Error('Falta la variable de entorno TEST_PASSWORD.')
const users = {
  adminA: 'migangel761+admina@gmail.com', guardA: 'migangel761+vigilantea@gmail.com', residentA: 'migangel761+residentea@gmail.com',
  adminB: 'migangel761+adminb@gmail.com', guardB: 'migangel761+vigilanteb@gmail.com', residentB: 'migangel761+residenteb@gmail.com',
}
const clients = Object.fromEntries(Object.keys(users).map(name => [name, createClient(url, key)]))
const result = { steps: [], failures: [] }
const record = (name, pass, detail) => { result.steps.push({ name, status: pass ? 'PASS' : 'FAIL', detail }); if (!pass) result.failures.push({ name, detail }) }

async function login(name) {
  const { data, error } = await clients[name].auth.signInWithPassword({ email: users[name], password })
  if (error || !data.user) throw new Error(`${name}: ${error?.message ?? 'sin usuario'}`)
  const { data: profile, error: profileError } = await clients[name].from('profiles').select('*').eq('id', data.user.id).single()
  if (profileError) throw profileError
  return profile
}

async function runTenant(label, guardName, residentName, unitNumber, complexId, unitId) {
  const guard = await login(guardName); const resident = await login(residentName)
  record(`${label}: perfiles`, guard.complex_id === complexId && resident.complex_id === complexId && resident.unit_id === unitId, JSON.stringify({ guard: guard.complex_id, resident: resident.complex_id, unit: resident.unit_id }))
  const path = `${complexId}/${randomUUID()}-${label.toLowerCase()}.jpg`
  const file = new Blob([Buffer.from(`RecepControl ${label} test evidence`)], { type: 'image/jpeg' })
  const { error: uploadError } = await clients[guardName].storage.from('receipt-photos').upload(path, file, { contentType: 'image/jpeg', upsert: false })
  record(`${label}: vigilante sube fotografía`, !uploadError, uploadError?.message ?? path)
  const reference = `E2E-DEV-${label}-001`
  const { data: receipt, error: insertError } = await clients[guardName].from('receipts').insert({ service_category: 'Paquete E2E', apartment: unitNumber, status: 'EN_PORTERIA', reference_code: reference, received_by: guard.id, complex_id: complexId, unit_id: unitId, resident_id: resident.id, guard_note: `Prueba controlada ${label}`, photo_path: path }).select('*').single()
  record(`${label}: paquete asociado`, !insertError && receipt?.complex_id === complexId && receipt?.unit_id === unitId && receipt?.resident_id === resident.id && receipt?.received_by === guard.id, insertError?.message ?? JSON.stringify(receipt))
  const { data: notificationRows, error: notificationError } = await clients[residentName].from('notifications').select('*').eq('receipt_id', receipt?.id)
  record(`${label}: una notificación para residente`, !notificationError && notificationRows?.length === 1 && notificationRows[0].recipient_id === resident.id, notificationError?.message ?? JSON.stringify(notificationRows))
  const { data: ownReceipt, error: ownReceiptError } = await clients[residentName].from('receipts').select('*').eq('id', receipt.id).maybeSingle()
  record(`${label}: residente consulta paquete`, !ownReceiptError && ownReceipt?.id === receipt.id, ownReceiptError?.message ?? JSON.stringify(ownReceipt))
  const { data: signed, error: signedError } = await clients[residentName].storage.from('receipt-photos').createSignedUrl(path, 120)
  record(`${label}: residente obtiene URL firmada`, !signedError && Boolean(signed?.signedUrl) && !signed.signedUrl.includes('/public/'), signedError?.message ?? signed?.signedUrl)
  const { error: deliveryError } = await clients[guardName].from('receipts').update({ status: 'ENTREGADO', delivered_at: new Date().toISOString() }).eq('id', receipt.id)
  record(`${label}: vigilante marca entrega`, !deliveryError, deliveryError?.message)
  const { data: delivered } = await clients[residentName].from('receipts').select('status').eq('id', receipt.id).single()
  record(`${label}: residente ve ENTREGADO`, delivered?.status === 'ENTREGADO', JSON.stringify(delivered))
  return { receiptId: receipt.id, path, guard, resident }
}

async function main() {
  const a = await runTenant('A', 'guardA', 'residentA', 'A-101', '18451c7c-338d-490b-934a-2f9f23e83cf9', '55eb14f4-891c-4cbb-a457-9b27580b582a')
  const b = await runTenant('B', 'guardB', 'residentB', 'B-101', 'd7eca568-df62-45fc-b8a2-3badd5d6fad4', 'a2d1f293-c7e1-4709-8249-e2caffc3604c')
  for (const [name, foreign] of [['residentA', b], ['residentB', a]]) {
    const { data, error } = await clients[name].from('receipts').select('*').eq('id', foreign.receiptId).maybeSingle(); record(`${name}: lectura recibo cruzado bloqueada`, !error && data === null, error?.message ?? JSON.stringify(data))
    const { data: unitData, error: unitError } = await clients[name].from('units').select('*').eq('id', foreign.resident.unit_id).maybeSingle(); record(`${name}: lectura unidad cruzada bloqueada`, !unitError && unitData === null, unitError?.message ?? JSON.stringify(unitData))
    const { data: noteData, error: noteError } = await clients[name].from('notifications').select('*').eq('recipient_id', foreign.resident.id).maybeSingle(); record(`${name}: notificación cruzada bloqueada`, !noteError && noteData === null, noteError?.message ?? JSON.stringify(noteData))
    const { data: photoData, error: photoError } = await clients[name].storage.from('receipt-photos').createSignedUrl(foreign.path, 120); record(`${name}: fotografía cruzada bloqueada`, Boolean(photoError) || !photoData?.signedUrl, photoError?.message ?? JSON.stringify(photoData))
  }
  const { data: crossUpdate, error: crossUpdateError } = await clients.residentA.from('receipts').update({ complex_id: 'd7eca568-df62-45fc-b8a2-3badd5d6fad4' }).eq('id', b.receiptId).select('id')
  record('Residente A no puede modificar recibo B', !crossUpdateError && (!crossUpdate || crossUpdate.length === 0), crossUpdateError?.message ?? JSON.stringify(crossUpdate))
  const { data: adminUnits, error: adminError } = await clients.adminA.from('units').select('*').eq('id', 'a2d1f293-c7e1-4709-8249-e2caffc3604c').maybeSingle(); record('Administrador A no puede leer unidad B', !adminError && adminUnits === null, adminError?.message ?? JSON.stringify(adminUnits))
  console.log(JSON.stringify(result, null, 2))
  if (result.failures.length) process.exitCode = 1
}
main().catch(error => { console.error(JSON.stringify({ fatal: error.message }, null, 2)); process.exitCode = 1 })
