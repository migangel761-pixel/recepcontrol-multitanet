import { createClient } from 'https://esm.sh/@supabase/supabase-js@2.57.0'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
}

function response(body: Record<string, unknown>, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
}

Deno.serve(async (request) => {
  if (request.method === 'OPTIONS') return new Response('ok', { headers: corsHeaders })
  if (request.method !== 'POST') return response({ error: 'Método no permitido.' }, 405)

  const supabaseUrl = Deno.env.get('SUPABASE_URL')
  const serviceRoleKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')
  const authorization = request.headers.get('Authorization')
  if (!supabaseUrl || !serviceRoleKey || !authorization) return response({ error: 'Solicitud no autorizada.' }, 401)

  const userClient = createClient(supabaseUrl, Deno.env.get('SUPABASE_ANON_KEY') ?? serviceRoleKey, { global: { headers: { Authorization: authorization } } })
  const { data: { user }, error: userError } = await userClient.auth.getUser()
  if (userError || !user) return response({ error: 'Sesión no válida.' }, 401)

  const adminClient = createClient(supabaseUrl, serviceRoleKey)
  const { data: adminProfile, error: adminProfileError } = await adminClient.from('profiles').select('id,role,complex_id').eq('id', user.id).single()
  if (adminProfileError || !adminProfile || adminProfile.role !== 'ADMINISTRADOR' || !adminProfile.complex_id) return response({ error: 'El usuario no es un administrador asignado a un conjunto.' }, 403)

  let payload: { full_name?: string; email?: string; unit_id?: string }
  try { payload = await request.json() } catch { return response({ error: 'El cuerpo de la solicitud no es válido.' }, 400) }
  const fullName = payload.full_name?.trim() ?? ''
  const email = payload.email?.trim().toLowerCase() ?? ''
  const unitId = payload.unit_id?.trim() ?? ''
  if (!fullName || !email || !unitId) return response({ error: 'Nombre, email y unidad son obligatorios.' }, 400)

  const { data: unit, error: unitError } = await adminClient.from('units').select('id,complex_id').eq('id', unitId).eq('complex_id', adminProfile.complex_id).single()
  if (unitError || !unit) return response({ error: 'La unidad no existe o no pertenece al conjunto del administrador.' }, 403)

  const { data: invited, error: inviteError } = await adminClient.auth.admin.inviteUserByEmail(email, { data: { full_name: fullName, role: 'RESIDENT' } })
  if (inviteError || !invited.user) {
    const duplicate = inviteError?.message?.toLowerCase().includes('already') || inviteError?.message?.toLowerCase().includes('exist')
    return response({ error: duplicate ? 'Ya existe un usuario con ese email.' : (inviteError?.message ?? 'No fue posible crear el usuario Auth.') }, duplicate ? 409 : 400)
  }

  const { data: profile, error: profileError } = await adminClient.from('profiles').update({ full_name: fullName, role: 'RESIDENT', complex_id: adminProfile.complex_id, unit_id: unit.id }).eq('id', invited.user.id).select('id,full_name,email,role,complex_id,unit_id').single()
  if (profileError || !profile) {
    await adminClient.auth.admin.deleteUser(invited.user.id)
    return response({ error: 'El usuario Auth fue creado, pero no fue posible asociarlo al perfil. La operación fue revertida.' }, 500)
  }

  return response({ user: invited.user.id, profile })
})
