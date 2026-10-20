import { Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './routes/ProtectedRoute';
import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import DonorDashboard from './pages/DonorDashboard';
import NgoDashboard from './pages/NgoDashboard';
import AdminDashboard from './pages/AdminDashboard';

export default function App() {
  return (
    <div className="app">
      <a href="#main" className="skip">Skip to content</a>
      <Navbar />
      <main id="main">
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/donor" element={<ProtectedRoute role="DONOR"><DonorDashboard /></ProtectedRoute>} />
          <Route path="/ngo" element={<ProtectedRoute role="NGO"><NgoDashboard /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="*" element={<div className="container section"><h1>Page not found</h1></div>} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
