import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = 'https://ubohhppltdadhxpagblc.supabase.co';
const SUPABASE_SECRET_KEY = process.env.SUPABASE_SECRET_KEY;
const NEW_PASSWORD = process.env.NEW_PASSWORD;

const USER_ID = '4fb5ea56-6bae-4f5f-b418-d895389cda08';

if (!SUPABASE_SECRET_KEY) {
  throw new Error('Falta SUPABASE_SECRET_KEY');
}

if (!NEW_PASSWORD) {
  throw new Error('Falta NEW_PASSWORD');
}

const supabase = createClient(SUPABASE_URL, SUPABASE_SECRET_KEY, {
  auth: {
    autoRefreshToken: false,
    persistSession: false
  }
});

const { data, error } = await supabase.auth.admin.updateUserById(
  USER_ID,
  {
    password: NEW_PASSWORD
  }
);

if (error) {
  throw new Error(`Error cambiando la contraseña: ${error.message}`);
}

console.log('Contraseña actualizada correctamente.');
console.log('Usuario:', data.user.email);