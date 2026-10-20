import { Sprout } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="brand"><Sprout size={20} /> TNSFRP</div>
        <p>Tamil Nadu Food Rescue &amp; Redistribution Platform — a college project connecting surplus food with the people who need it.</p>
      </div>
    </footer>
  );
}
