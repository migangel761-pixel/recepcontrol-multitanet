export type Role = 'ADMINISTRADOR' | 'VIGILANTE' | 'RESIDENT'

export type Profile = {
  id: string
  full_name: string
  email: string
  phone: string | null
  role: Role
  apartment: string | null
  complex_id: string | null
  unit_id: string | null
}

export type Receipt = {
  id: string
  service_category: string
  apartment: string
  status: 'EN_PORTERIA' | 'ENTREGADO'
  reference_code: string
  received_at: string
  delivered_at: string | null
  guard_note: string
  photo_path: string | null
  photo_url?: string | null
  resident_id: string | null
}

export type Notification = {
  id: string
  title: string
  message: string
  created_at: string
  read_at: string | null
  receipt_id: string | null
}

export type Announcement = {
  id: string
  title: string
  content: string
  created_at: string
  author_id: string
}
