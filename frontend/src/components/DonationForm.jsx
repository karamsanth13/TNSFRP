import { useEffect, useState } from 'react';
import api, { errMsg, getDistricts } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { toLocalInput } from '../services/format';
import { Alert } from './ui';
import Geo from './Geo';

const CATEGORIES = ['Cooked meals', 'Rice & grains', 'Bakery', 'Fruits & vegetables', 'Snacks', 'Beverages', 'Other'];
const UNITS = ['KG', 'LITRE', 'PACKETS', 'MEALS'];

export default function DonationForm({ onCreated, onCancel }) {
  const { user } = useAuth();
  const now = new Date();
  const [districts, setDistricts] = useState([]);
  const [f, setF] = useState({
    foodTitle: '', foodType: CATEGORIES[0], description: '', quantity: '', unit: 'MEALS',
    preparedAt: toLocalInput(now), expiryAt: toLocalInput(new Date(now.getTime() + 4 * 3600 * 1000)),
    pickupAddress: user.address || '', district: user.district || '', latitude: user.latitude ?? '', longitude: user.longitude ?? '',
  });
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => { getDistricts().then(setDistricts).catch(() => {}); }, []);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setErr('');
    if (new Date(f.expiryAt) <= new Date()) return setErr('Expiry time must be in the future.');
    if (new Date(f.preparedAt) >= new Date(f.expiryAt)) return setErr('Preparation time must be before expiry time.');
    setBusy(true);
    try {
      await api.post('/donor/listings', {
        ...f, quantity: Number(f.quantity), latitude: Number(f.latitude), longitude: Number(f.longitude),
        preparedAt: new Date(f.preparedAt).toISOString(), expiryAt: new Date(f.expiryAt).toISOString(),
      });
      onCreated();
    } catch (e2) { setErr(errMsg(e2)); }
    finally { setBusy(false); }
  };

  return (
    <form className="card form-card" onSubmit={submit}>
      <h2>New food donation</h2>
      <Alert>{err}</Alert>
      <div className="grid-2">
        <div className="field"><label htmlFor="title">Food title</label><input id="title" required maxLength={100} value={f.foodTitle} onChange={set('foodTitle')} placeholder="Vegetable biryani" /></div>
        <div className="field"><label htmlFor="cat">Category</label>
          <select id="cat" value={f.foodType} onChange={set('foodType')}>{CATEGORIES.map((c) => <option key={c}>{c}</option>)}</select></div>
        <div className="field"><label htmlFor="qty">Quantity</label><input id="qty" type="number" min="0.1" step="any" required value={f.quantity} onChange={set('quantity')} /></div>
        <div className="field"><label htmlFor="unit">Unit</label>
          <select id="unit" value={f.unit} onChange={set('unit')}>{UNITS.map((u) => <option key={u}>{u}</option>)}</select></div>
        <div className="field"><label htmlFor="prep">Prepared at</label><input id="prep" type="datetime-local" required value={f.preparedAt} onChange={set('preparedAt')} /></div>
        <div className="field"><label htmlFor="exp">Expires at</label><input id="exp" type="datetime-local" required value={f.expiryAt} onChange={set('expiryAt')} /></div>
      </div>
      <div className="field"><label htmlFor="desc">Description (optional)</label><textarea id="desc" rows={2} maxLength={500} value={f.description} onChange={set('description')} /></div>
      <div className="grid-2">
        <div className="field"><label htmlFor="addr">Pickup address</label><input id="addr" required value={f.pickupAddress} onChange={set('pickupAddress')} /></div>
        <div className="field"><label htmlFor="dist">District</label>
          <select id="dist" required value={f.district} onChange={set('district')}><option value="">Select district</option>{districts.map((d) => <option key={d}>{d}</option>)}</select></div>
      </div>
      <Geo lat={f.latitude} lng={f.longitude} onChange={(latitude, longitude) => setF({ ...f, latitude, longitude })} />
      <div className="form-actions">
        <button type="button" className="btn btn-ghost" onClick={onCancel}>Cancel</button>
        <button className="btn btn-primary" disabled={busy}>{busy ? 'Posting…' : 'Post donation'}</button>
      </div>
    </form>
  );
}
