import { Clock, MapPin, Navigation, Package, Store } from 'lucide-react';
import { StatusBadge } from './ui';
import { fmtDate, timeLeft } from '../services/format';

export default function ListingCard({ listing: l, distanceKm, children }) {
  const live = l.status === 'POSTED' || l.status === 'ACCEPTED';
  return (
    <article className="card listing">
      <div className="listing-top">
        <div>
          <h3>{l.foodTitle}</h3>
          <span className="muted">{l.foodType}</span>
        </div>
        <div className="listing-badges">
          {distanceKm !== undefined && <span className="badge badge-dist"><Navigation size={12} /> {distanceKm} km</span>}
          <StatusBadge status={l.status} />
        </div>
      </div>
      {l.description && <p className="listing-desc">{l.description}</p>}
      <ul className="meta">
        <li><Package size={15} /> {l.quantity} {l.unit}</li>
        <li><Store size={15} /> {l.donorName}</li>
        <li><MapPin size={15} /> {l.pickupAddress}, {l.district}</li>
        <li><Clock size={15} /> {live ? `${timeLeft(l.expiryAt)} · ` : ''}expires {fmtDate(l.expiryAt)}</li>
      </ul>
      {children && <div className="listing-actions">{children}</div>}
    </article>
  );
}
