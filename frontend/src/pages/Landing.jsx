import { Link } from 'react-router-dom';
import { ArrowRight, Clock, HeartHandshake, Leaf, MapPin, Recycle, Truck, Utensils } from 'lucide-react';
import ListingCard from '../components/ListingCard';

const future = (h) => new Date(Date.now() + h * 3600 * 1000).toISOString();
// DEMONSTRATION DATA – not from the database.
const DEMO = [
  { id: 'd1', foodTitle: 'Sambar rice & curd rice', foodType: 'Cooked meals', quantity: 60, unit: 'MEALS', donorName: 'Sample Wedding Hall', pickupAddress: 'Anna Nagar', district: 'Chennai', status: 'POSTED', expiryAt: future(5), description: 'Surplus from an evening reception.' },
  { id: 'd2', foodTitle: 'Bread & buns', foodType: 'Bakery', quantity: 25, unit: 'KG', donorName: 'Sample Bakery Canteen', pickupAddress: 'RS Puram', district: 'Coimbatore', status: 'POSTED', expiryAt: future(9), description: 'Packed this morning.' },
  { id: 'd3', foodTitle: 'Veg biryani packets', foodType: 'Cooked meals', quantity: 40, unit: 'PACKETS', donorName: 'Sample Hostel Mess', pickupAddress: 'KK Nagar', district: 'Madurai', status: 'POSTED', expiryAt: future(3), description: 'Freshly cooked lunch surplus.' },
];
const STEPS = [
  [Utensils, 'Donors post', 'Restaurants, hostels and halls list surplus food with quantity and expiry time.'],
  [MapPin, 'NGOs discover', 'Approved NGOs see donations within their service radius, sorted by distance.'],
  [Truck, 'Pickup', 'An NGO accepts, collects the food and confirms the pickup.'],
  [HeartHandshake, 'Served', 'The donor confirms the handover and the meal reaches someone who needs it.'],
];

export default function Landing() {
  return (
    <>
      <section className="hero">
        <div className="container hero-inner">
          <div className="hero-text">
            <span className="eyebrow"><Leaf size={14} /> Tamil Nadu · 38 districts</span>
            <h1>Surplus food should reach a plate, <em>not a bin.</em></h1>
            <p className="lead">TNSFRP connects restaurants, hostels, canteens and marriage halls with verified NGOs that can collect surplus food before it spoils.</p>
            <div className="hero-cta">
              <Link to="/register" className="btn btn-primary btn-lg">Donate food <ArrowRight size={18} /></Link>
              <Link to="/register" className="btn btn-accent btn-lg">Join as an NGO</Link>
            </div>
          </div>
          <div className="hero-art" aria-hidden="true">
            <div className="blob" />
            <div className="float-card c1"><Utensils size={18} /> 60 meals posted</div>
            <div className="float-card c2"><Clock size={18} /> Expires in 5 h</div>
            <div className="float-card c3"><MapPin size={18} /> 3.2 km away</div>
          </div>
        </div>
      </section>

      <section className="section container">
        <h2 className="section-title">Our mission</h2>
        <p className="lead narrow">Every day, good food is thrown away while people nearby go hungry. We make the hand-off simple: post, match, collect — within hours.</p>
      </section>

      <section className="section band">
        <div className="container">
          <h2 className="section-title">How it works</h2>
          <div className="grid-4">
            {STEPS.map(([Icon, t, d], i) => (
              <div className="card step" key={t}><span className="step-no">{i + 1}</span><Icon size={26} /><h3>{t}</h3><p>{d}</p></div>
            ))}
          </div>
        </div>
      </section>

      <section className="section container">
        <h2 className="section-title">Impact <span className="demo-tag">Demonstration figures</span></h2>
        <div className="grid-4 impact">
          {[['12,400', 'meals rescued'], ['180', 'partner donors'], ['64', 'verified NGOs'], ['38', 'districts covered']].map(([n, l]) => (
            <div key={l}><div className="big">{n}</div><div className="muted">{l}</div></div>
          ))}
        </div>
      </section>

      <section className="section container">
        <h2 className="section-title">Featured donations <span className="demo-tag">Demo examples</span></h2>
        <div className="grid-3">{DEMO.map((l) => <ListingCard key={l.id} listing={l} />)}</div>
      </section>

      <section className="section band">
        <div className="container sustain">
          <Recycle size={40} />
          <div>
            <h2>Good for people, good for the planet</h2>
            <p>Food waste releases methane in landfills and wastes the water, energy and labour used to grow and cook it. Redistributing surplus food cuts that waste and feeds communities.</p>
          </div>
        </div>
      </section>
    </>
  );
}
