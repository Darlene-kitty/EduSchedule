import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Calendar, 
  Menu, 
  Bell, 
  Edit2, 
  Save, 
  X, 
  User, 
  Mail, 
  Phone, 
  CalendarDays,
  ChevronRight,
  BookOpen,
  Users,
  Activity,
  School,
  AlertTriangle,
  LogOut,
  Check
} from 'lucide-react';
import './ProfilePage.css';

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  createdAt: string;
}

const ProfilePage: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: ''
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserProfile = async () => {
    try {
      const token = localStorage.getItem('token');
      const username = localStorage.getItem('username');
      
      if (!username) {
        setError('Utilisateur non connecté');
        setLoading(false);
        return;
      }

      const response = await fetch(`/api/v1/users/username/${username}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const userData = await response.json();
        setUser(userData);
        setFormData({
          firstName: userData.firstName || '',
          lastName: userData.lastName || '',
          email: userData.email || '',
          phone: userData.phone || ''
        });
      } else {
        setError('Erreur lors du chargement du profil');
      }
    } catch (err) {
      setError('Erreur de connexion');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!user) return;

    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/v1/users/${user.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        const updatedUser = await response.json();
        setUser(updatedUser);
        setIsEditing(false);
        setSuccess('Profil mis à jour avec succès !');
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => setSuccess(''), 4000);
      } else {
        setError('Erreur lors de la mise à jour');
      }
    } catch (err) {
      setError('Erreur de connexion');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  const handleCancel = () => {
    setFormData({
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      email: user?.email || '',
      phone: user?.phone || ''
    });
    setIsEditing(false);
    setError('');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-green-50/30 to-orange-50/20 flex items-center justify-center">
        <div className="text-lg text-gray-600 font-semibold">Chargement...</div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 via-green-50/30 to-orange-50/20 flex items-center justify-center">
        <div className="text-lg text-red-600 font-semibold">Utilisateur non trouvé</div>
      </div>
    );
  }

  const memberSinceDate = new Date(user.createdAt);
  const monthsSince = Math.floor((Date.now() - memberSinceDate.getTime()) / (1000 * 60 * 60 * 24 * 30));

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-green-50/30 to-orange-50/20">
      
      {/* Header */}
      <header className="bg-white/80 backdrop-blur-xl sticky top-0 z-50 shadow-sm border-b border-gray-200 animate-fade-in">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <button className="lg:hidden">
                <Menu className="w-6 h-6 text-gray-600" />
              </button>
              <div className="flex items-center gap-3">
                <div className="w-11 h-11 bg-gradient-to-br from-orange-400 to-orange-600 rounded-xl flex items-center justify-center shadow-lg">
                  <Calendar className="w-6 h-6 text-white" />
                </div>
                <div>
                  <h1 className="text-xl font-bold text-gray-900">EduSchedule</h1>
                  <p className="text-xs text-gray-500 font-medium">Gestion Académique</p>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="hidden md:flex items-center gap-2 px-3 py-1.5 bg-white rounded-full shadow-sm">
                <CalendarDays className="w-4 h-4 text-gray-500" />
                <span className="text-sm text-gray-700 font-medium">
                  {new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'short' })}
                </span>
              </div>
              <div className="px-3 py-1.5 bg-white rounded-full shadow-sm">
                <span className="text-sm font-bold text-gray-900">
                  {new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
              <div className="relative">
                <button className="p-2 hover:bg-white rounded-full transition-colors">
                  <Bell className="w-5 h-5 text-gray-600" />
                </button>
                <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full animate-pulse"></span>
              </div>
              <div className="flex items-center gap-2 ml-2">
                <div className="w-9 h-9 bg-gradient-to-br from-green-500 to-green-700 rounded-full flex items-center justify-center text-white text-sm font-bold shadow-lg">
                  {user.firstName?.[0]?.toUpperCase() || user.username[0]?.toUpperCase()}
                </div>
                <span className="text-sm font-semibold text-gray-900 hidden sm:block">
                  {user.firstName || user.username}
                </span>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-6 py-8">
        
        {/* Page Title */}
        <div className="mb-8 animate-slide-in-up">
          <div className="flex items-center justify-between flex-wrap gap-4">
            <div>
              <h2 className="text-3xl font-bold text-gray-900 mb-1">Mon Profil</h2>
              <p className="text-gray-600 font-medium">Gérez vos informations et préférences</p>
            </div>
            {!isEditing && (
              <button 
                onClick={() => setIsEditing(true)}
                className="flex items-center gap-2 bg-gradient-to-r from-green-500 to-green-700 text-white px-6 py-3 rounded-xl hover:shadow-xl transition-all duration-300 font-semibold"
              >
                <Edit2 className="w-5 h-5" />
                Modifier le profil
              </button>
            )}
          </div>
        </div>

        {/* Alert Success */}
        {success && (
          <div className="mb-6 p-4 bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-xl animate-scale-in">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                <Check className="w-5 h-5 text-white" />
              </div>
              <div className="text-green-800 font-semibold">{success}</div>
            </div>
          </div>
        )}

        {/* Alert Error */}
        {error && (
          <div className="mb-6 p-4 bg-gradient-to-r from-red-50 to-orange-50 border border-red-200 rounded-xl">
            <div className="text-red-800 font-semibold">{error}</div>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Left Column - Profile Card */}
          <div className="lg:col-span-1">
            <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl overflow-hidden border border-gray-200 animate-slide-in-up delay-100">
              {/* Profile Header with Gradient */}
              <div className="bg-gradient-to-br from-green-500 to-green-700 p-6 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -mr-16 -mt-16"></div>
                <div className="absolute bottom-0 left-0 w-24 h-24 bg-white opacity-10 rounded-full -ml-12 -mb-12"></div>
                <div className="relative">
                  <div className="w-24 h-24 bg-white rounded-2xl flex items-center justify-center text-green-600 text-3xl font-bold shadow-2xl mx-auto mb-4">
                    {user.firstName?.[0]?.toUpperCase() || user.username[0]?.toUpperCase()}
                  </div>
                  <h3 className="text-xl font-bold text-white text-center mb-1">
                    {user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : user.username}
                  </h3>
                  <p className="text-green-100 text-center font-medium">@{user.username}</p>
                  <div className="flex items-center justify-center gap-2 mt-3">
                    <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-xs text-white font-semibold">Enseignant</span>
                    <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-xs text-white font-semibold">Actif</span>
                  </div>
                </div>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-3 gap-3 p-6 border-b border-gray-100">
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900 mb-1">12</div>
                  <div className="text-xs text-gray-600 font-medium">Cours</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900 mb-1">24h</div>
                  <div className="text-xs text-gray-600 font-medium">Semaine</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-gray-900 mb-1">3</div>
                  <div className="text-xs text-gray-600 font-medium">Écoles</div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="p-6 space-y-2">
                <button className="w-full flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-gray-50 transition-colors text-left">
                  <div className="w-10 h-10 bg-green-100 rounded-xl flex items-center justify-center">
                    <Calendar className="w-5 h-5 text-green-600" />
                  </div>
                  <div className="flex-1">
                    <div className="text-sm font-semibold text-gray-900">Mon emploi du temps</div>
                    <div className="text-xs text-gray-500">Voir la semaine</div>
                  </div>
                  <ChevronRight className="w-5 h-5 text-gray-400" />
                </button>
                <button className="w-full flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-gray-50 transition-colors text-left">
                  <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
                    <BookOpen className="w-5 h-5 text-orange-600" />
                  </div>
                  <div className="flex-1">
                    <div className="text-sm font-semibold text-gray-900">Mes cours</div>
                    <div className="text-xs text-gray-500">Liste complète</div>
                  </div>
                  <ChevronRight className="w-5 h-5 text-gray-400" />
                </button>
                <button className="w-full flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-gray-50 transition-colors text-left">
                  <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
                    <Users className="w-5 h-5 text-blue-600" />
                  </div>
                  <div className="flex-1">
                    <div className="text-sm font-semibold text-gray-900">Mes étudiants</div>
                    <div className="text-xs text-gray-500">245 étudiants</div>
                  </div>
                  <ChevronRight className="w-5 h-5 text-gray-400" />
                </button>
              </div>
            </div>

            {/* Activity Card */}
            <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl p-6 mt-6 border border-gray-200 animate-slide-in-up delay-200">
              <h4 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Activity className="w-5 h-5 text-green-600" />
                Activité récente
              </h4>
              <div className="space-y-6">
                <div className="relative pl-8">
                  <div className="absolute left-0 top-3 w-4 h-4 bg-gradient-to-br from-green-500 to-green-700 rounded-full border-3 border-white shadow-lg"></div>
                  <div className="text-sm font-semibold text-gray-900 mb-1">Cours modifié</div>
                  <div className="text-xs text-gray-500">Machine Learning - M1</div>
                  <div className="text-xs text-gray-400 mt-1">Il y a 2 heures</div>
                </div>
                <div className="relative pl-8">
                  <div className="absolute left-0 top-3 w-4 h-4 bg-gradient-to-br from-orange-400 to-orange-600 rounded-full border-3 border-white shadow-lg"></div>
                  <div className="text-sm font-semibold text-gray-900 mb-1">Salle réservée</div>
                  <div className="text-xs text-gray-500">Lab Info 2</div>
                  <div className="text-xs text-gray-400 mt-1">Il y a 5 heures</div>
                </div>
                <div className="relative pl-8">
                  <div className="absolute left-0 top-3 w-4 h-4 bg-blue-500 rounded-full border-3 border-white shadow-lg"></div>
                  <div className="text-sm font-semibold text-gray-900 mb-1">Profil actualisé</div>
                  <div className="text-xs text-gray-500">Informations personnelles</div>
                  <div className="text-xs text-gray-400 mt-1">Hier</div>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column - Information Cards */}
          <div className="lg:col-span-2 space-y-6">
            
            {/* Personal Information */}
            <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl overflow-hidden border border-gray-200 animate-slide-in-up delay-300">
              <div className="px-6 py-5 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white">
                <h3 className="font-bold text-gray-900 text-lg flex items-center gap-2">
                  <User className="w-5 h-5 text-green-600" />
                  Informations personnelles
                </h3>
              </div>

              <div className="p-6">
                {/* Display Mode */}
                {!isEditing ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="p-4 bg-gradient-to-br from-gray-50 to-white rounded-xl border border-gray-100 hover:bg-gradient-to-br hover:from-green-50 hover:to-emerald-50 transition-all hover:translate-x-1">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <User className="w-5 h-5 text-green-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs text-gray-500 font-medium mb-1">Prénom</p>
                          <p className="font-semibold text-gray-900 truncate">{user.firstName || 'Non renseigné'}</p>
                        </div>
                      </div>
                    </div>

                    <div className="p-4 bg-gradient-to-br from-gray-50 to-white rounded-xl border border-gray-100 hover:bg-gradient-to-br hover:from-green-50 hover:to-emerald-50 transition-all hover:translate-x-1">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <User className="w-5 h-5 text-green-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs text-gray-500 font-medium mb-1">Nom</p>
                          <p className="font-semibold text-gray-900 truncate">{user.lastName || 'Non renseigné'}</p>
                        </div>
                      </div>
                    </div>

                    <div className="p-4 bg-gradient-to-br from-gray-50 to-white rounded-xl border border-gray-100 hover:bg-gradient-to-br hover:from-orange-50 hover:to-amber-50 transition-all hover:translate-x-1">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <Mail className="w-5 h-5 text-orange-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs text-gray-500 font-medium mb-1">Email</p>
                          <p className="font-semibold text-gray-900 truncate">{user.email}</p>
                        </div>
                      </div>
                    </div>

                    <div className="p-4 bg-gradient-to-br from-gray-50 to-white rounded-xl border border-gray-100 hover:bg-gradient-to-br hover:from-blue-50 hover:to-cyan-50 transition-all hover:translate-x-1">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <Phone className="w-5 h-5 text-blue-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs text-gray-500 font-medium mb-1">Téléphone</p>
                          <p className="font-semibold text-gray-900 truncate">{user.phone || 'Non renseigné'}</p>
                        </div>
                      </div>
                    </div>

                    <div className="p-4 bg-gradient-to-br from-gray-50 to-white rounded-xl border border-gray-100 md:col-span-2 hover:bg-gradient-to-br hover:from-purple-50 hover:to-pink-50 transition-all hover:translate-x-1">
                      <div className="flex items-start gap-3">
                        <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                          <CalendarDays className="w-5 h-5 text-purple-600" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-xs text-gray-500 font-medium mb-1">Membre depuis</p>
                          <p className="font-semibold text-gray-900 truncate">
                            {memberSinceDate.toLocaleDateString('fr-FR')} · {monthsSince} mois
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  /* Edit Mode */
                  <form onSubmit={handleSubmit}>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                      <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                          Prénom
                        </label>
                        <input
                          type="text"
                          name="firstName"
                          value={formData.firstName}
                          onChange={handleInputChange}
                          className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-transparent outline-none transition-all font-medium"
                          placeholder="Votre prénom"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                          Nom
                        </label>
                        <input
                          type="text"
                          name="lastName"
                          value={formData.lastName}
                          onChange={handleInputChange}
                          className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-transparent outline-none transition-all font-medium"
                          placeholder="Votre nom"
                        />
                      </div>
                    </div>

                    <div className="space-y-6 mb-6">
                      <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                          Email professionnel
                        </label>
                        <input
                          type="email"
                          name="email"
                          value={formData.email}
                          onChange={handleInputChange}
                          className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-transparent outline-none transition-all font-medium"
                          placeholder="votre.email@iusjc.cm"
                          required
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                          Téléphone
                        </label>
                        <input
                          type="tel"
                          name="phone"
                          value={formData.phone}
                          onChange={handleInputChange}
                          className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-transparent outline-none transition-all font-medium"
                          placeholder="+237 6XX XXX XXX"
                        />
                      </div>
                    </div>

                    <div className="flex gap-3">
                      <button
                        type="submit"
                        className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-green-500 to-green-700 text-white px-6 py-3 rounded-xl hover:shadow-xl transition-all duration-300 font-semibold"
                      >
                        <Save className="w-5 h-5" />
                        Enregistrer les modifications
                      </button>
                      <button
                        type="button"
                        onClick={handleCancel}
                        className="flex items-center gap-2 bg-gray-100 text-gray-700 px-6 py-3 rounded-xl hover:bg-gray-200 transition-all duration-300 font-semibold"
                      >
                        <X className="w-5 h-5" />
                        Annuler
                      </button>
                    </div>
                  </form>
                )}
              </div>
            </div>

            {/* Schools & Stats */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 animate-slide-in-up delay-400">
              <div className="bg-white/90 backdrop-blur-xl p-6 rounded-2xl shadow-lg border border-gray-100 hover:shadow-xl hover:-translate-y-1 transition-all">
                <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mb-4">
                  <School className="w-6 h-6 text-green-600" />
                </div>
                <div className="text-3xl font-bold text-gray-900 mb-1">SJI</div>
                <div className="text-sm text-gray-600 font-medium">8 cours actifs</div>
              </div>

              <div className="bg-white/90 backdrop-blur-xl p-6 rounded-2xl shadow-lg border border-gray-100 hover:shadow-xl hover:-translate-y-1 transition-all">
                <div className="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center mb-4">
                  <School className="w-6 h-6 text-orange-600" />
                </div>
                <div className="text-3xl font-bold text-gray-900 mb-1">SJM</div>
                <div className="text-sm text-gray-600 font-medium">3 cours actifs</div>
              </div>

              <div className="bg-white/90 backdrop-blur-xl p-6 rounded-2xl shadow-lg border border-gray-100 hover:shadow-xl hover:-translate-y-1 transition-all">
                <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mb-4">
                  <School className="w-6 h-6 text-blue-600" />
                </div>
                <div className="text-3xl font-bold text-gray-900 mb-1">PrepaVogt</div>
                <div className="text-sm text-gray-600 font-medium">1 cours actif</div>
              </div>
            </div>

            {/* Danger Zone */}
            <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl overflow-hidden border-2 border-red-100 animate-slide-in-up delay-400">
              <div className="px-6 py-5 bg-gradient-to-r from-red-50 to-orange-50 border-b border-red-100">
                <h3 className="font-bold text-red-900 text-lg flex items-center gap-2">
                  <AlertTriangle className="w-5 h-5 text-red-600" />
                  Zone de sécurité
                </h3>
              </div>
              <div className="p-6">
                <p className="text-gray-600 mb-4 font-medium">
                  Attention : Cette action mettra fin à votre session actuelle.
                </p>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-2 bg-gradient-to-r from-red-500 to-red-600 text-white px-6 py-3 rounded-xl hover:shadow-xl transition-all duration-300 font-semibold"
                >
                  <LogOut className="w-5 h-5" />
                  Se déconnecter
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default ProfilePage;