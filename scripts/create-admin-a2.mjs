import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = 'https://ubohhppltdadhxpagblc.supabase.co';
const SUPABASE_SECRET_KEY = process.env.SUPABASE_SECRET_KEY;
const TEST_PASSWORD = process.env.TEST_PASSWORD;

if (!SUPABASE_SECRET_KEY) {
  throw new Error('Falta SUPABASE_SECRET_KEY');
}

if (!TEST_PASSWORD) {
  throw new Error('Falta TEST_PASSWORD');
}

const supabase = createClient(
  SUPABASE_URL,
  SUPABASE_SECRET_KEY,
  {
    auth: {
      autoRefreshToken: false,
      persistSession: false
    }
  }
);

const email = 'migangel761+admina2@gmail.com';
const fullName = 'Administrador A2';
const complexId = '18451c7c-338d-490b-934a-2f9f23e83cf9';

console.log('Creando Administrador A2...');

const { data: authData, error: authError } =
  await supabase.auth.admin.createUser({
    email,
    password: TEST_PASSWORD,
    email_confirm: true,
    user_metadata: {
      full_name: fullName,
      role: 'ADMINISTRADOR'
    }
  });

if (authError) {
  throw new Error(`Error creando usuario Auth: ${authError.message}`);
}

const userId = authData.user.id;

console.log(`Usuario Auth creado: ${userId}`);

const { data: profileData, error: profileError } = await supabase
  .from('profiles')
  .update({
    full_name: fullName,
    email,
    role: 'ADMINISTRADOR',
    complex_id: complexId,
    unit_id: null
  })
  .eq('id', userId)
  .select('id, full_name, email, role, complex_id, unit_id')
  .single();

if (profileError) {
  console.error('Falló la asociación del perfil.');
  console.error(profileError.message);

  const { error: deleteError } =
    await supabase.auth.admin.deleteUser(userId);

  if (deleteError) {
    console.error(
      `Además, no se pudo eliminar el usuario Auth: ${deleteError.message}`
    );
  } else {
    console.log('Usuario Auth eliminado como compensación.');
  }

  throw new Error('No se pudo completar la creación del administrador.');
}

console.log('');
console.log('Administrador A2 creado correctamente.');
console.log('');
console.log(profileData);
console.log('');
console.log(`Email: ${email}`);
console.log('Rol: ADMINISTRADOR');
console.log(`Complex ID: ${complexId}`);
console.log('Unidad: NULL');
console.log('');
console.log('IMPORTANTE: esta cuenta es únicamente para pruebas DEV.');
