import { createClient } from '@supabase/supabase-js'

const SUPABASE_URL = 'https://ubohhppltdadhxpagblc.supabase.co'

const EMAIL = 'migangel761+residentea2@gmail.com'
const PASSWORD = 'TestOnly-2026!R'

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
console.log(' CREACIÓN CONTROLADA - RESIDENTE A2')
console.log('==========================================')
console.log('')
console.log(`Correo: ${EMAIL}`)
console.log('')

console.log('1. Comprobando si el correo ya existe...')

const {
  data: existingUsers,
  error: listError
} = await supabaseAdmin.auth.admin.listUsers({
  page: 1,
  perPage: 100
})

if (listError) {
  console.error('Error consultando Auth:', listError.message)
  process.exit(1)
}

const existing = existingUsers.users.find(
  user => user.email?.toLowerCase() === EMAIL.toLowerCase()
)

if (existing) {
  console.log('')
  console.log('EL USUARIO YA EXISTE.')
  console.log(`ID: ${existing.id}`)
  console.log(`Email: ${existing.email}`)
  console.log('')
  console.log('No se realizó ninguna modificación.')
  process.exit(0)
}

console.log('Correo disponible.')
console.log('')

console.log('2. Creando usuario mediante Supabase Auth...')

const {
  data,
  error
} = await supabaseAdmin.auth.admin.createUser({
  email: EMAIL,
  password: PASSWORD,
  email_confirm: true,
  user_metadata: {
    full_name: 'Residente A2',
    role: 'RESIDENT'
  }
})

if (error) {
  console.error('')
  console.error('NO SE PUDO CREAR EL USUARIO.')
  console.error('Error:', error.message)
  process.exit(1)
}

console.log('')
console.log('==========================================')
console.log(' USUARIO CREADO CORRECTAMENTE')
console.log('==========================================')
console.log('')
console.log(`ID: ${data.user.id}`)
console.log(`Email: ${data.user.email}`)
console.log(`Confirmado: ${data.user.email_confirmed_at ? 'Sí' : 'No'}`)
console.log('')
console.log('No se realizó ninguna otra modificación.')
console.log('')