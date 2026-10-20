import { useState } from 'react';
import { LocateFixed } from 'lucide-react';

/** Latitude/longitude inputs with a "use my location" helper. */
export default function Geo({ lat, lng, onChange }) {
  const [msg, setMsg] = useState('');
  const locate = () => {
    if (!navigator.geolocation) return setMsg('Location is not supported by this browser.');
    setMsg('Locating…');
    navigator.geolocation.getCurrentPosition(
      (p) => { onChange(p.coords.latitude.toFixed(5), p.coords.longitude.toFixed(5)); setMsg('Location filled in.'); },
      () => setMsg('Could not get your location. Enter it manually.')
    );
  };
  return (
    <div className="geo">
      <div className="field"><label htmlFor="lat">Latitude</label>
        <input id="lat" type="number" step="any" min="-90" max="90" required value={lat} onChange={(e) => onChange(e.target.value, lng)} placeholder="13.0827" /></div>
      <div className="field"><label htmlFor="lng">Longitude</label>
        <input id="lng" type="number" step="any" min="-180" max="180" required value={lng} onChange={(e) => onChange(lat, e.target.value)} placeholder="80.2707" /></div>
      <div className="geo-btn">
        <button type="button" className="btn btn-ghost btn-sm" onClick={locate}><LocateFixed size={15} /> Use my location</button>
        {msg && <small className="muted">{msg}</small>}
      </div>
    </div>
  );
}
