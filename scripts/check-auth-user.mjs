import { createClient } from '@supabase/supabase-js'

const SUPABASE_URL = 'https://ubohhppltdadhxpagblc.supabase.co'
const SEARCH_EMAIL = 'migangel761+vigilantea@gmail.com'

const SECRET_KEY = process.env.SUPABASE_SECRET_KEY

if (!SECRET_KEY) {
  console.error('ERROR: No se encontró SUPABASE_SECRET_KEY.')
  process.exit(1)
}

const supabaseAdmin = createClient(
  SUPABASE_URL,
  SECRET_KEY,
  {
    auth: {
      autoRefreshToken: false,
      persistSession: false
    }
  }
)

console.log('')
console.log('==========================================')
console.log(' DIAGNÓSTICO AUTH - BÚSQUEDA POR EMAIL')
console.log('==========================================')
console.log('')
console.log(`Correo buscado: ${SEARCH_EMAIL}`)
console.log('')
console.log('Consultando usuarios de Auth...')

const {
  data,
  error
} = await supabaseAdmin.auth.admin.listUsers({
  page: 1,
  perPage: 100
})

if (error) {
  console.error('')
  console.error('ERROR AL CONSULTAR AUTH:')
  console.error(error.message)
  process.exit(1)
}

const users = data?.users ?? []

const matches = users.filter(
  user => user.email?.toLowerCase() === SEARCH_EMAIL.toLowerCase()
)

console.log('')
console.log(`Usuarios devueltos por Auth: ${users.length}`)
console.log(`Coincidencias exactas: ${matches.length}`)
console.log('')

if (matches.length === 0) {
  console.log('RESULTADO: Auth NO encuentra el usuario por correo.')
} else {
  for (const user of matches) {
    console.log('RESULTADO: Auth SÍ encuentra el usuario.')
    console.log(`ID: ${user.id}`)
    console.log(`Email: ${user.email}`)
    console.log(`Confirmado: ${user.email_confirmed_at ? 'Sí' : 'No'}`)
    console.log(`Último acceso: ${user.last_sign_in_at ?? 'Nunca'}`)
  }
}

console.log('')
console.log('No se realizó ninguna modificación.')
console.log('')
