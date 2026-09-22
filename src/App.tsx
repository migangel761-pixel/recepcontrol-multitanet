import { FormEvent, useEffect, useState } from 'react'
import { supabase } from './lib/supabase'
import type { Announcement, Notification, Profile, Receipt } from './types'

type LoginRole = 'VIGILANTE' | 'RESIDENT'

function Login({ onLoggedIn }: { onLoggedIn: (profile: Profile) => void }) {
  const [role, setRole] = useState<LoginRole>('VIGILANTE')
  const [registering, setRegistering] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [phone, setPhone] = useState('')
  const [fullName, setFullName] = useState('')
  const [apartment, setApartment] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function selectRole(nextRole: LoginRole) {
    setRole(nextRole)
    setEmail('')
    setPhone('')
  }

  async function submit(event: FormEvent) {
    event.preventDefault(); setError(''); setMessage(''); setLoading(true)
    if (registering) {
      const { error: signUpError } = await supabase.auth.signUp({ email: email.trim(), password, options: { data: { full_name: fullName.trim(), phone: phone.trim(), apartment: apartment.trim() } } })
      if (signUpError) setError(signUpError.message); else setMessage('Cuenta creada. Revisa tu correo para confirmar y luego inicia sesión.')
      setLoading(false); return
    }
    const { data, error: authError } = await supabase.auth.signInWithPassword({ email: email.trim(), password })
    if (authError || !data.user) { setError(authError?.message ?? 'No fue posible iniciar sesión.'); setLoading(false); return }
    const { data: profile, error: profileError } = await supabase.from('profiles').select('*').eq('id', data.user.id).single()
    if (profileError) setError(profileError.message)
    else if (!profile || !['ADMINISTRADOR', role].includes(profile.role)) setError('Este usuario no tiene el perfil de acceso seleccionado.')
    else onLoggedIn(profile as Profile)
    setLoading(false)
  }

  return <main className="login-page"><section className="login-wrap">
    <div className="brand"><div className="brand-mark"><img src="/assets/recepcontrol-logo.jpg" alt="Emblema RecepControl" /></div><h1>RecepControl</h1><p>Recepción de correspondencia y novedades</p></div>
    <section className="quick-card"><div className="section-heading"><strong>Accesos rápidos de prueba</strong><span>1 Toque</span></div><div className="quick-actions"><button className="quick guard" onClick={() => selectRole('VIGILANTE')}><b>◈</b><span><strong>Demo Vigilante</strong><small>Ingresar como Vigilante</small></span></button><button className="quick resident" onClick={() => selectRole('RESIDENT')}><b>⌂</b><span><strong>Demo Residente</strong><small>Ingresar como Residente</small></span></button></div></section>
    <section className="login-card"><div className="login-card-head"><div className="head-icon">▣</div><div><strong>Iniciar Sesión</strong><small>Seleccione su perfil de acceso</small></div></div><form onSubmit={submit}><div className="role-tabs"><button type="button" className={role === 'VIGILANTE' ? 'active guard-tab' : ''} onClick={() => selectRole('VIGILANTE')}>◈ Vigilante</button><button type="button" className={role === 'RESIDENT' ? 'active resident-tab' : ''} onClick={() => selectRole('RESIDENT')}>⌂ Residente</button></div><label>Correo<input type="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="ejemplo@recepcontrol.com" /></label><label>Contraseña<div className="password-field"><input type={showPassword ? 'text' : 'password'} required value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" /><button type="button" onClick={() => setShowPassword(value => !value)}>{showPassword ? 'Ocultar' : 'Mostrar'}</button></div></label><label>Celular<input type="tel" value={phone} onChange={e => setPhone(e.target.value)} placeholder="310 123 4567" /></label>{registering && <><label>Nombre completo<input required value={fullName} onChange={e => setFullName(e.target.value)} /></label><label>Apartamento<input required value={apartment} onChange={e => setApartment(e.target.value)} /></label></>}{error && <p className="error">{error}</p>}{message && <p className="success">{message}</p>}<button className={`submit ${role === 'VIGILANTE' ? 'submit-guard' : 'submit-resident'}`} disabled={loading}>{loading ? 'Procesando...' : registering ? 'Crear cuenta de residente  →' : `Ingresar como ${role === 'VIGILANTE' ? 'Vigilante' : 'Residente'}  →`}</button><button className="link-button" type="button" onClick={() => { setRegistering(value => !value); setRole('RESIDENT') }}>{registering ? 'Ya tengo una cuenta' : 'Crear cuenta de residente'}</button></form></section><p className="footer-note">◈ Sistema de Control de Portería · RecepControl</p>
  </section></main>
}

function AdminPanel({ profile, onSignOut }: { profile: Profile; onSignOut: () => void }) {
  const [units, setUnits] = useState<{ id: string; unit_number: string; building: string | null; type: string | null }[]>([])
  const [members, setMembers] = useState<{ id: string; full_name: string; email: string; role: string; unit_id: string | null }[]>([])
  const [unitNumber, setUnitNumber] = useState(''); const [building, setBuilding] = useState(''); const [type, setType] = useState('Apartamento'); const [memberId, setMemberId] = useState(''); const [memberRole, setMemberRole] = useState('RESIDENT'); const [memberUnit, setMemberUnit] = useState(''); const [residentName, setResidentName] = useState(''); const [residentEmail, setResidentEmail] = useState(''); const [residentUnit, setResidentUnit] = useState(''); const [announcementTitle, setAnnouncementTitle] = useState(''); const [announcementContent, setAnnouncementContent] = useState(''); const [error, setError] = useState(''); const [message, setMessage] = useState(''); const [saving, setSaving] = useState(false)
  useEffect(() => { void loadUnits(); void loadMembers() }, [profile.complex_id])
  async function loadUnits() { const { data, error: queryError } = await supabase.from('units').select('id,unit_number,building,type').eq('complex_id', profile.complex_id).order('unit_number'); if (queryError) setError(queryError.message); setUnits(data ?? []) }
  async function loadMembers() { const { data, error: queryError } = await supabase.from('profiles').select('id,full_name,email,role,unit_id').eq('complex_id', profile.complex_id).order('full_name'); if (queryError) setError(queryError.message); setMembers(data ?? []) }
  async function createUnit(event: FormEvent) { event.preventDefault(); setSaving(true); setError(''); const { error: insertError } = await supabase.from('units').insert({ complex_id: profile.complex_id, unit_number: unitNumber.trim(), building: building.trim() || null, type }); if (insertError) setError(insertError.message); else { setUnitNumber(''); setBuilding(''); await loadUnits() }; setSaving(false) }
  async function deleteUnit(id: string) { const { error: deleteError } = await supabase.from('units').delete().eq('id', id); if (deleteError) setError(deleteError.message); else await loadUnits() }
  async function assignMember(event: FormEvent) { event.preventDefault(); setSaving(true); setError(''); const { error: rpcError } = await supabase.rpc('admin_update_member', { target_id: memberId, new_role: memberRole, new_unit_id: memberUnit || null }); if (rpcError) setError(rpcError.message); else await loadMembers(); setSaving(false) }
  async function createResident(event: FormEvent) { event.preventDefault(); setSaving(true); setError(''); setMessage(''); const { data, error: functionError } = await supabase.functions.invoke('admin-create-resident', { body: { full_name: residentName.trim(), email: residentEmail.trim(), unit_id: residentUnit } }); if (functionError) setError(functionError.message); else if (data?.error) setError(data.error); else { setResidentName(''); setResidentEmail(''); setResidentUnit(''); setMessage('Residente creado. Se envió una invitación para establecer su contraseña.'); await loadMembers() }; setSaving(false) }
  async function publishAnnouncement(event: FormEvent) { event.preventDefault(); setSaving(true); setError(''); const { error: insertError } = await supabase.from('announcements').insert({ title: announcementTitle.trim(), content: announcementContent.trim(), author_id: profile.id, complex_id: profile.complex_id }); if (insertError) setError(insertError.message); else { setAnnouncementTitle(''); setAnnouncementContent('') }; setSaving(false) }
  return <main className="app-shell"><header><div><p className="eyebrow">RECEPCONTROL MULTITENANT</p><h1>Panel administrador</h1><p className="muted">{profile.full_name} · Gestión de su conjunto residencial</p></div><button className="secondary" onClick={onSignOut}>Cerrar sesión</button></header>{error && <p className="error">{error}</p>}{message && <p className="success">{message}</p>}<section className="metrics"><article><strong>{units.length}</strong><span>Unidades registradas</span></article><article><strong>{members.length}</strong><span>Usuarios del conjunto</span></article><article><strong>3</strong><span>Roles disponibles</span></article></section><section className="panel action-panel"><h2>Nuevo residente</h2><p className="muted">Se enviará una invitación para que establezca su contraseña.</p><form className="receipt-form" onSubmit={createResident}><label>Nombre completo<input required value={residentName} onChange={e => setResidentName(e.target.value)} /></label><label>Email<input required type="email" value={residentEmail} onChange={e => setResidentEmail(e.target.value)} /></label><label>Unidad<select required value={residentUnit} onChange={e => setResidentUnit(e.target.value)}><option value="">Seleccionar unidad</option>{units.map(unit => <option key={unit.id} value={unit.id}>{unit.unit_number}{unit.building ? ` · ${unit.building}` : ''}</option>)}</select></label><button disabled={saving || units.length === 0}>{saving ? 'Creando...' : 'Crear residente'}</button></form></section><section className="panel action-panel"><h2>Agregar unidad</h2><form className="receipt-form" onSubmit={createUnit}><label>Número de unidad<input required value={unitNumber} onChange={e => setUnitNumber(e.target.value)} placeholder="Número de unidad" /></label><label>Edificio o torre<input value={building} onChange={e => setBuilding(e.target.value)} placeholder="Edificio o torre" /></label><label>Tipo<select value={type} onChange={e => setType(e.target.value)}><option>Apartamento</option><option>Casa</option><option>Local</option><option>Zona común</option></select></label><button disabled={saving}>{saving ? 'Guardando...' : 'Agregar unidad'}</button></form></section><section className="panel action-panel"><h2>Asignar rol y unidad</h2><p className="muted">Solo usuarios ya creados y pertenecientes a este conjunto.</p><form className="receipt-form" onSubmit={assignMember}><label>Usuario<select required value={memberId} onChange={e => setMemberId(e.target.value)}><option value="">Seleccionar usuario</option>{members.map(member => <option key={member.id} value={member.id}>{member.full_name || member.email}</option>)}</select></label><label>Rol<select value={memberRole} onChange={e => setMemberRole(e.target.value)}><option value="RESIDENT">Residente</option><option value="VIGILANTE">Vigilante</option><option value="ADMINISTRADOR">Administrador</option></select></label><label>Unidad<select value={memberUnit} onChange={e => setMemberUnit(e.target.value)}><option value="">Sin unidad</option>{units.map(unit => <option key={unit.id} value={unit.id}>{unit.unit_number}</option>)}</select></label><button disabled={saving || !memberId}>{saving ? 'Guardando...' : 'Guardar asignación'}</button></form></section><section className="panel action-panel"><h2>Publicar comunicado</h2><form className="receipt-form" onSubmit={publishAnnouncement}><label>Título<input required value={announcementTitle} onChange={e => setAnnouncementTitle(e.target.value)} placeholder="Mantenimiento de zonas comunes" /></label><label>Mensaje<textarea required value={announcementContent} onChange={e => setAnnouncementContent(e.target.value)} placeholder="Escribe el comunicado para tu conjunto" /></label><button disabled={saving}>{saving ? 'Publicando...' : 'Publicar comunicado'}</button></form></section><section className="panel"><h2>Unidades del conjunto</h2>{units.length === 0 ? <p className="muted">No hay unidades registradas.</p> : units.map(unit => <article className="list-item" key={unit.id}><div><strong>{unit.unit_number}</strong><p>{unit.building ?? 'Sin edificio'} · {unit.type ?? 'Sin tipo'}</p></div><button className="mini-button danger-button" onClick={() => void deleteUnit(unit.id)}>Eliminar</button></article>)}</section></main>
}

function Dashboard({ profile, onSignOut }: { profile: Profile; onSignOut: () => void }) {
  const [receipts, setReceipts] = useState<Receipt[]>([]); const [notifications, setNotifications] = useState<Notification[]>([]); const [announcements, setAnnouncements] = useState<Announcement[]>([]); const [error, setError] = useState('')
  const [units, setUnits] = useState<{ id: string; unit_number: string; building: string | null; type: string | null }[]>([]); const [unitId, setUnitId] = useState(''); const [serviceCategory, setServiceCategory] = useState('Paquete o Encomienda en Portería'); const [guardNote, setGuardNote] = useState(''); const [photo, setPhoto] = useState<File | null>(null); const [photoPreview, setPhotoPreview] = useState(''); const [saving, setSaving] = useState(false)
  useEffect(() => { void loadUnits(); void load() }, [profile.id, profile.complex_id])
  async function loadUnits() {
    if (!profile.complex_id) { setUnits([]); return }
    const { data, error: unitsError } = await supabase.from('units').select('id,unit_number,building,type').eq('complex_id', profile.complex_id).order('unit_number')
    if (unitsError) setError(unitsError.message)
    setUnits(data ?? [])
  }
  async function load() {
    const receiptQuery = profile.role === 'RESIDENT' ? supabase.from('receipts').select('*').eq('resident_id', profile.id).order('created_at', { ascending: false }) : supabase.from('receipts').select('*').eq('complex_id', profile.complex_id).order('created_at', { ascending: false })
    const [{ data: receiptData, error: receiptError }, { data: notificationData, error: notificationError }, { data: announcementData, error: announcementError }] = await Promise.all([receiptQuery, supabase.from('notifications').select('*').eq('recipient_id', profile.id).order('created_at', { ascending: false }), supabase.from('announcements').select('*').eq('complex_id', profile.complex_id).order('created_at', { ascending: false })])
    if (receiptError || notificationError || announcementError) setError(receiptError?.message ?? notificationError?.message ?? announcementError?.message ?? '')
    const loadedReceipts = (receiptData ?? []) as Receipt[]
    const withPhotoUrls = await Promise.all(loadedReceipts.map(async receipt => {
      if (!receipt.photo_path) return receipt
      const { data } = await supabase.storage.from('receipt-photos').createSignedUrl(receipt.photo_path, 3600)
      return { ...receipt, photo_url: data?.signedUrl ?? null }
    }))
    setReceipts(withPhotoUrls); setNotifications((notificationData ?? []) as Notification[]); setAnnouncements((announcementData ?? []) as Announcement[])
  }
  async function createReceipt(event: FormEvent) {
    event.preventDefault(); setError(''); setSaving(true)
    if (!profile.complex_id) { setError('Tu perfil no tiene un conjunto asignado.'); setSaving(false); return }
    if (!unitId) { setError('Selecciona una unidad antes de registrar el evento.'); setSaving(false); return }
    const unit = units.find(item => item.id === unitId)
    if (!unit) { setError('La unidad seleccionada no pertenece a tu conjunto.'); setSaving(false); return }
    const { data: resident, error: residentError } = await supabase.from('profiles').select('id').eq('complex_id', profile.complex_id).eq('unit_id', unit.id).eq('role', 'RESIDENT').maybeSingle()
    if (residentError) { setError(residentError.message); setSaving(false); return }
    if (!resident) { setError('La unidad seleccionada no tiene un residente asociado.'); setSaving(false); return }
    let photoPath: string | null = null
    if (photo) {
      photoPath = `${profile.complex_id}/${crypto.randomUUID()}-${photo.name.replace(/[^a-zA-Z0-9._-]/g, '_')}`
      const { error: uploadError } = await supabase.storage.from('receipt-photos').upload(photoPath, photo, { contentType: photo.type, upsert: false })
      if (uploadError) { setError(uploadError.message); setSaving(false); return }
    }
    const { error: insertError } = await supabase.from('receipts').insert({ service_category: serviceCategory, apartment: unit.unit_number, reference_code: `REC-${Math.floor(100000 + Math.random() * 900000)}`, received_by: profile.id, complex_id: profile.complex_id, unit_id: unit.id, resident_id: resident.id, guard_note: guardNote.trim() || 'Registrado en bitácora de portería.', photo_path: photoPath })
    if (insertError) { if (photoPath) await supabase.storage.from('receipt-photos').remove([photoPath]); setError(insertError.message) } else { setUnitId(''); setGuardNote(''); setPhoto(null); setPhotoPreview(''); await load() }
    setSaving(false)
  }
  async function markDelivered(id: string) { const { error: updateError } = await supabase.from('receipts').update({ status: 'ENTREGADO', delivered_at: new Date().toISOString() }).eq('id', id); if (updateError) setError(updateError.message); else await load() }
  const isStaff = profile.role !== 'RESIDENT'
  return <main className="app-shell"><header><div><p className="eyebrow">RECEPCONTROL MULTITENANT</p><h1>Panel {profile.role.toLowerCase()}</h1><p className="muted">{profile.full_name} · {profile.email}</p></div><button className="secondary" onClick={onSignOut}>Cerrar sesión</button></header>{error && <p className="error">{error}</p>}<section className="metrics"><article><strong>{receipts.length}</strong><span>Registros visibles</span></article><article><strong>{notifications.length}</strong><span>Notificaciones</span></article><article><strong>{announcements.length}</strong><span>Comunicados</span></article></section>{isStaff && <section className="panel action-panel"><h2>Registrar evento / correspondencia</h2><p className="muted">El residente correspondiente recibirá una notificación interna.</p><form className="receipt-form" onSubmit={createReceipt}><label>Tipo de evento<select value={serviceCategory} onChange={e => setServiceCategory(e.target.value)}><option>Paquete o Encomienda en Portería</option><option>Cuota de Administración Mensual</option><option>Recibo de Servicio Público</option><option>Comunicado o Novedad General</option></select></label><label>Unidad o apartamento<select required value={unitId} onChange={e => setUnitId(e.target.value)} disabled={units.length === 0}><option value="">{units.length === 0 ? 'No hay unidades disponibles' : 'Seleccionar unidad'}</option>{units.map(unit => <option key={unit.id} value={unit.id}>{unit.unit_number}{unit.building ? ` · ${unit.building}` : ''}</option>)}</select>{units.length === 0 && <small className="field-help">Este conjunto no tiene unidades registradas.</small>}</label><label>Fotografía de evidencia<input type="file" accept="image/*" capture="environment" onChange={e => { const file = e.target.files?.[0] ?? null; setPhoto(file); setPhotoPreview(file ? URL.createObjectURL(file) : '') }} />{photoPreview && <img className="photo-preview" src={photoPreview} alt="Vista previa de evidencia" />}</label><label>Observación<textarea value={guardNote} onChange={e => setGuardNote(e.target.value)} placeholder="Detalles del registro" /></label><button disabled={saving || units.length === 0}>{saving ? 'Guardando...' : 'Registrar evento'}</button></form></section>}<div className="grid"><section className="panel"><h2>Paquetes y recibos</h2>{receipts.length === 0 ? <p className="muted">No hay registros disponibles.</p> : receipts.map(receipt => <article className="list-item" key={receipt.id}><div><strong>{receipt.service_category}</strong><p>{receipt.apartment} · {receipt.reference_code}<br />{new Date(receipt.received_at).toLocaleString('es-CO')}</p>{receipt.photo_url && <img className="receipt-photo" src={receipt.photo_url} alt="Evidencia del recibo" />}</div><div className="item-actions"><span className={receipt.status === 'ENTREGADO' ? 'badge done' : 'badge'}>{receipt.status.replace('_', ' ')}</span>{isStaff && receipt.status !== 'ENTREGADO' && <button className="mini-button" onClick={() => void markDelivered(receipt.id)}>Marcar entregado</button>}</div></article>)}</section><section className="panel"><h2>Notificaciones</h2>{notifications.length === 0 ? <p className="muted">No tienes notificaciones.</p> : notifications.map(notification => <article className="list-item" key={notification.id}><div><strong>{notification.title}</strong><p>{notification.message}</p></div></article>)}</section></div><section className="panel announcements-panel"><h2>Comunicados del conjunto</h2>{announcements.length === 0 ? <p className="muted">No hay comunicados publicados.</p> : announcements.map(announcement => <article className="announcement" key={announcement.id}><strong>{announcement.title}</strong><p>{announcement.content}</p><small>{new Date(announcement.created_at).toLocaleString('es-CO')}</small></article>)}</section></main>
}

export function App() {
  const [profile, setProfile] = useState<Profile | null>(null)
  useEffect(() => { void supabase.auth.getSession().then(async ({ data }) => { if (!data.session) return; const { data: p } = await supabase.from('profiles').select('*').eq('id', data.session.user.id).single(); if (p) setProfile(p as Profile) }) }, [])
  if (!profile) return <Login onLoggedIn={setProfile} />
  if (profile.role === 'ADMINISTRADOR') return <AdminPanel profile={profile} onSignOut={async () => { await supabase.auth.signOut(); setProfile(null) }} />
  return <Dashboard profile={profile} onSignOut={async () => { await supabase.auth.signOut(); setProfile(null) }} />
}
