import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { HeartHandshake, Utensils } from 'lucide-react';
import api, { errMsg, getDistricts } from '../services/api';
import { Alert } from '../components/ui';
import Geo from '../components/Geo';

const ORG = [['RESTAURANT', 'Restaurant'], ['HOSTEL', 'Hostel'], ['CANTEEN', 'Canteen'], ['MARRIAGE_HALL', 'Marriage hall']];

export default function Register() {
  const nav = useNavigate();
  const [districts, setDistricts] = useState([]);
  const [role, setRole] = useState('DONOR');
  const [f, setF] = useState({
    fullName: '', email: '', password: '', phone: '', organizationType: 'RESTAURANT', district: '', address: '',
    latitude: '', longitude: '', ngoName: '', registrationNumber: '', serviceRadiusKm: 10, capacityPerDay: 100,
  });
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => { getDistricts().then(setDistricts).catch(() => setErr('Could not load districts. Is the backend running?')); }, []);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setErr('');
    if (f.password.length < 8) return setErr('Password must be at least 8 characters.');
    setBusy(true);
    try {
      const body = { ...f, role, latitude: Number(f.latitude), longitude: Number(f.longitude),
        serviceRadiusKm: Number(f.serviceRadiusKm), capacityPerDay: Number(f.capacityPerDay) };
      if (role === 'DONOR') { delete body.ngoName; delete body.registrationNumber; } else delete body.organizationType;
      await api.post('/auth/register', body);
      nav('/login', { state: { registered: true } });
    } catch (e2) { setErr(errMsg(e2)); }
    finally { setBusy(false); }
  };

  return (
    <section className="auth-wrap">
      <form className="card auth-card wide" onSubmit={submit}>
        <h1>Join TNSFRP</h1>
        <div className="role-pick" role="radiogroup" aria-label="Account type">
          <button type="button" role="radio" aria-checked={role === 'DONOR'} className={role === 'DONOR' ? 'role active' : 'role'} onClick={() => setRole('DONOR')}>
            <Utensils size={20} /><b>Food donor</b><small>Restaurant, hostel, canteen, hall</small></button>
          <button type="button" role="radio" aria-checked={role === 'NGO'} className={role === 'NGO' ? 'role active' : 'role'} onClick={() => setRole('NGO')}>
            <HeartHandshake size={20} /><b>NGO</b><small>Collect and distribute food</small></button>
        </div>
        <Alert>{err}</Alert>
        <div className="grid-2">
          <div className="field"><label htmlFor="n">{role === 'NGO' ? 'Contact person' : 'Organisation / owner name'}</label><input id="n" required value={f.fullName} onChange={set('fullName')} /></div>
          <div className="field"><label htmlFor="e">Email</label><input id="e" type="email" required autoComplete="email" value={f.email} onChange={set('email')} /></div>
          <div className="field"><label htmlFor="p">Password</label><input id="p" type="password" required minLength={8} autoComplete="new-password" value={f.password} onChange={set('password')} /></div>
          <div className="field"><label htmlFor="ph">Phone</label><input id="ph" required value={f.phone} onChange={set('phone')} placeholder="9876543210" /></div>
          {role === 'DONOR' ? (
            <div className="field"><label htmlFor="ot">Donor type</label>
              <select id="ot" value={f.organizationType} onChange={set('organizationType')}>{ORG.map(([v, l]) => <option key={v} value={v}>{l}</option>)}</select></div>
          ) : (
            <>
              <div className="field"><label htmlFor="nn">NGO name</label><input id="nn" required value={f.ngoName} onChange={set('ngoName')} /></div>
              <div className="field"><label htmlFor="rn">Registration number</label><input id="rn" required value={f.registrationNumber} onChange={set('registrationNumber')} /></div>
              <div className="field"><label htmlFor="sr">Service radius (km)</label><input id="sr" type="number" min="1" required value={f.serviceRadiusKm} onChange={set('serviceRadiusKm')} /></div>
              <div className="field"><label htmlFor="cp">Capacity per day (meals)</label><input id="cp" type="number" min="1" required value={f.capacityPerDay} onChange={set('capacityPerDay')} /></div>
            </>
          )}
          <div className="field"><label htmlFor="d">District</label>
            <select id="d" required value={f.district} onChange={set('district')}><option value="">Select district</option>{districts.map((d) => <option key={d}>{d}</option>)}</select></div>
          <div className="field"><label htmlFor="a">Address</label><input id="a" required value={f.address} onChange={set('address')} /></div>
        </div>
        <Geo lat={f.latitude} lng={f.longitude} onChange={(latitude, longitude) => setF({ ...f, latitude, longitude })} />
        <button className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
        <p className="muted center">Already registered? <Link to="/login">Log in</Link></p>
      </form>
    </section>
  );
}
