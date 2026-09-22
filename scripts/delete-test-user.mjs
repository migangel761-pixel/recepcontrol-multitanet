import { createClient } from '@supabase/supabase-js'

const SUPABASE_URL = 'https://ubohhppltdadhxpagblc.supabase.co'

const USER_ID = '141ae842-73f3-43c6-afde-441a120c275b'
const EXPECTED_EMAIL = 'migangel761+vigilantea@gmail.com'

const SECRET_KEY = process.env.SUPABASE_SECRET_KEY

if (!SECRET_KEY) {
  console.error('ERROR: No se encontró SUPABASE_SECRET_KEY.')
  console.error('No se ejecutó ninguna operación.')
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
console.log(' ELIMINACIÓN CONTROLADA - SUPABASE DEV')
console.log('==========================================')
console.log('')
console.log('Usuario objetivo:')
console.log(`ID:    ${USER_ID}`)
console.log(`Email: ${EXPECTED_EMAIL}`)
console.log('')

console.log('1. Verificando que el usuario exista...')

const {
  data: userResult,
  error: lookupError
} = await supabaseAdmin.auth.admin.getUserById(USER_ID)

if (lookupError) {
  console.error('')
  console.error('NO SE PUDO LOCALIZAR EL USUARIO.')
  console.error('Error:', lookupError.message)
  console.error('')
  process.exit(1)
}

if (!userResult?.user) {
  console.error('')
  console.error('El usuario no fue encontrado.')
  console.error('No se realizó ninguna eliminación.')
  console.error('')
  process.exit(1)
}

const foundEmail = userResult.user.email

console.log(`Usuario encontrado: ${foundEmail}`)

if (foundEmail !== EXPECTED_EMAIL) {
  console.error('')
  console.error('SEGURIDAD: el ID corresponde a otro correo.')
  console.error(`Esperado: ${EXPECTED_EMAIL}`)
  console.error(`Encontrado: ${foundEmail}`)
  console.error('')
  console.error('NO SE REALIZÓ NINGUNA ELIMINACIÓN.')
  process.exit(1)
}

console.log('')
console.log('2. El usuario coincide con Vigilante A.')
console.log('')
console.log('3. Ejecutando auth.admin.deleteUser()...')

const {
  data: deleteResult,
  error: deleteError
} = await supabaseAdmin.auth.admin.deleteUser(USER_ID)

if (deleteError) {
  console.error('')
  console.error('==========================================')
  console.error(' NO SE ELIMINÓ EL USUARIO')
  console.error('==========================================')
  console.error('')
  console.error('Error:', deleteError.message)
  console.error('')
  process.exit(1)
}

console.log('')
console.log('==========================================')
console.log(' USUARIO ELIMINADO CORRECTAMENTE')
console.log('==========================================')
console.log('')
console.log(`ID eliminado: ${USER_ID}`)
console.log(`Correo: ${EXPECTED_EMAIL}`)
console.log('')
console.log('No se realizaron otras operaciones.')
console.log('')