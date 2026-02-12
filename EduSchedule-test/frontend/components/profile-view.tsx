"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { useAuth } from "@/contexts/auth-context"
import { useToast } from "@/hooks/use-toast"
import { profileService, type ProfileData, type UserPreferences } from "@/services/profile.service"
import { 
  Mail, 
  Edit3, 
  Save, 
  X,
  Camera,
  Key,
  Bell,
  Globe,
  User,
  MapPin,
  Phone,
  GraduationCap,
  BookOpen,
  Calendar,
  Clock,
  Award,
  TrendingUp,
  Settings,
  Shield,
  Eye,
  EyeOff
} from "lucide-react"

export function ProfileView() {
  const { user } = useAuth()
  const { toast } = useToast()
  const [isEditing, setIsEditing] = useState(false)
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [showCurrentPassword, setShowCurrentPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  
  // États pour les informations du profil
  const [profileData, setProfileData] = useState({
    name: user?.name || "",
    email: user?.email || "",
    phone: "",
    address: "",
    bio: "",
    department: "",
    specialization: "",
  })

  // États pour le changement de mot de passe
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  })

  // États pour les préférences
  const [preferences, setPreferences] = useState({
    emailNotifications: true,
    pushNotifications: true,
    language: "fr",
    theme: "light",
  })

  // Charger les données du profil au montage
  useEffect(() => {
    loadProfileData()
  }, [])

  const loadProfileData = async () => {
    try {
      // Ici on pourrait charger les données depuis le backend
      // const profile = await profileService.getCurrentProfile()
      // setProfileData(profile)
    } catch (error) {
      console.error('Erreur chargement profil:', error)
    }
  }

  const handleSaveProfile = async () => {
    try {
      await profileService.updateProfile(profileData)
      
      toast({
        title: "Profil mis à jour",
        description: "Vos informations ont été sauvegardées avec succès",
        variant: "default",
      })
      setIsEditing(false)
    } catch (error) {
      toast({
        title: "Erreur",
        description: "Impossible de sauvegarder le profil",
        variant: "destructive",
      })
    }
  }

  const handleChangePassword = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast({
        title: "Erreur",
        description: "Les mots de passe ne correspondent pas",
        variant: "destructive",
      })
      return
    }

    try {
      await profileService.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
        confirmPassword: passwordData.confirmPassword,
      })
      
      toast({
        title: "Mot de passe modifié",
        description: "Votre mot de passe a été mis à jour avec succès",
        variant: "default",
      })
      setIsChangingPassword(false)
      setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" })
    } catch (error) {
      toast({
        title: "Erreur",
        description: "Impossible de modifier le mot de passe",
        variant: "destructive",
      })
    }
  }

  const getRoleDisplayName = (role: string) => {
    switch (role) {
      case "admin": return "Administrateur"
      case "teacher": return "Enseignant"
      case "student": return "Étudiant"
      default: return role
    }
  }

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case "admin": return "bg-red-100 text-red-800"
      case "teacher": return "bg-blue-100 text-blue-800"
      case "student": return "bg-green-100 text-green-800"
      default: return "bg-gray-100 text-gray-800"
    }
  }

  return (
    <div className="flex h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <Sidebar activePage="profile" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header 
          title="Mon Profil" 
          subtitle="Gérez vos informations personnelles et préférences" 
        />

        <main className="flex-1 overflow-y-auto p-6">
          <div className="max-w-6xl mx-auto space-y-8">
            
            {/* En-tête du profil avec design moderne */}
            <div className="relative">
              {/* Bannière de fond */}
              <div className="h-48 bg-gradient-to-r from-[#15803D] via-[#16a34a] to-[#22c55e] rounded-2xl relative overflow-hidden">
                <div className="absolute inset-0 bg-black/10"></div>
                <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-black/20 to-transparent"></div>
              </div>
              
              {/* Informations du profil */}
              <div className="relative -mt-20 px-8">
                <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-6">
                  <div className="flex flex-col md:flex-row md:items-end gap-6">
                    {/* Avatar */}
                    <div className="relative group">
                      <div className="w-32 h-32 rounded-2xl bg-white p-2 shadow-xl">
                        <img 
                          src={user?.avatar || "/placeholder.svg"} 
                          alt={user?.name} 
                          className="w-full h-full rounded-xl object-cover"
                        />
                      </div>
                      <button className="absolute bottom-2 right-2 bg-[#15803D] text-white p-2.5 rounded-xl hover:bg-[#166534] transition-all duration-200 shadow-lg group-hover:scale-110">
                        <Camera className="w-4 h-4" />
                      </button>
                    </div>
                    
                    {/* Informations de base */}
                    <div className="text-white md:mb-4">
                      <h1 className="text-3xl font-bold mb-2">{user?.name}</h1>
                      <div className="flex flex-wrap items-center gap-3 mb-3">
                        <Badge 
                          variant="secondary" 
                          className={`px-4 py-1.5 text-sm font-medium ${getRoleBadgeColor(user?.role || "")} border-0`}
                        >
                          {getRoleDisplayName(user?.role || "")}
                        </Badge>
                        <div className="flex items-center gap-2 text-white/90">
                          <Mail className="w-4 h-4" />
                          <span className="text-sm">{user?.email}</span>
                        </div>
                      </div>
                      <p className="text-white/80 text-sm">
                        Membre depuis {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('fr-FR', { 
                          year: 'numeric', 
                          month: 'long' 
                        }) : 'récemment'}
                      </p>
                    </div>
                  </div>
                  
                  {/* Actions */}
                  <div className="flex gap-3 md:mb-4">
                    <Button
                      onClick={() => setIsEditing(!isEditing)}
                      variant={isEditing ? "outline" : "default"}
                      className={`${isEditing 
                        ? "bg-white/10 border-white/20 text-white hover:bg-white/20" 
                        : "bg-white text-[#15803D] hover:bg-gray-50"
                      } px-6 py-2.5 rounded-xl font-medium transition-all duration-200`}
                    >
                      {isEditing ? (
                        <>
                          <X className="w-4 h-4 mr-2" />
                          Annuler
                        </>
                      ) : (
                        <>
                          <Edit3 className="w-4 h-4 mr-2" />
                          Modifier
                        </>
                      )}
                    </Button>
                    <Button
                      variant="outline"
                      className="bg-white/10 border-white/20 text-white hover:bg-white/20 px-6 py-2.5 rounded-xl font-medium"
                    >
                      <Settings className="w-4 h-4 mr-2" />
                      Paramètres
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            <div className="grid lg:grid-cols-3 gap-8">
              
              {/* Colonne principale - Informations personnelles */}
              <div className="lg:col-span-2 space-y-6">
                
                {/* Informations personnelles */}
                <Card className="p-8 shadow-sm border-0 bg-white/80 backdrop-blur-sm rounded-2xl">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-[#15803D]/10 rounded-xl">
                      <User className="w-5 h-5 text-[#15803D]" />
                    </div>
                    <h3 className="text-xl font-semibold text-gray-900">Informations personnelles</h3>
                  </div>
                  
                  <div className="grid md:grid-cols-2 gap-6">
                    <div className="space-y-5">
                      <div>
                        <Label htmlFor="name" className="text-sm font-medium text-gray-700 mb-2 block">
                          Nom complet
                        </Label>
                        <div className="relative">
                          <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                          <Input
                            id="name"
                            value={profileData.name}
                            onChange={(e) => setProfileData({...profileData, name: e.target.value})}
                            disabled={!isEditing}
                            className="pl-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                        </div>
                      </div>
                      
                      <div>
                        <Label htmlFor="email" className="text-sm font-medium text-gray-700 mb-2 block">
                          Email
                        </Label>
                        <div className="relative">
                          <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                          <Input
                            id="email"
                            type="email"
                            value={profileData.email}
                            onChange={(e) => setProfileData({...profileData, email: e.target.value})}
                            disabled={!isEditing}
                            className="pl-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                        </div>
                      </div>
                      
                      <div>
                        <Label htmlFor="phone" className="text-sm font-medium text-gray-700 mb-2 block">
                          Téléphone
                        </Label>
                        <div className="relative">
                          <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                          <Input
                            id="phone"
                            value={profileData.phone}
                            onChange={(e) => setProfileData({...profileData, phone: e.target.value})}
                            disabled={!isEditing}
                            placeholder="+237 6XX XXX XXX"
                            className="pl-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                        </div>
                      </div>
                    </div>

                    <div className="space-y-5">
                      <div>
                        <Label htmlFor="department" className="text-sm font-medium text-gray-700 mb-2 block">
                          Département
                        </Label>
                        <div className="relative">
                          <GraduationCap className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                          <Input
                            id="department"
                            value={profileData.department}
                            onChange={(e) => setProfileData({...profileData, department: e.target.value})}
                            disabled={!isEditing}
                            placeholder="Ex: Informatique, Mathématiques..."
                            className="pl-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                        </div>
                      </div>
                      
                      <div>
                        <Label htmlFor="specialization" className="text-sm font-medium text-gray-700 mb-2 block">
                          Spécialisation
                        </Label>
                        <div className="relative">
                          <BookOpen className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                          <Input
                            id="specialization"
                            value={profileData.specialization}
                            onChange={(e) => setProfileData({...profileData, specialization: e.target.value})}
                            disabled={!isEditing}
                            placeholder="Ex: Développement Web, Algèbre..."
                            className="pl-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                        </div>
                      </div>
                      
                      <div>
                        <Label htmlFor="address" className="text-sm font-medium text-gray-700 mb-2 block">
                          Adresse
                        </Label>
                        <div className="relative">
                          <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                          <Input
                            id="address"
                            value={profileData.address}
                            onChange={(e) => setProfileData({...profileData, address: e.target.value})}
                            disabled={!isEditing}
                            placeholder="Votre adresse"
                            className="pl-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="mt-6">
                    <Label htmlFor="bio" className="text-sm font-medium text-gray-700 mb-2 block">
                      Biographie
                    </Label>
                    <Textarea
                      id="bio"
                      value={profileData.bio}
                      onChange={(e) => setProfileData({...profileData, bio: e.target.value})}
                      disabled={!isEditing}
                      placeholder="Parlez-nous de vous, vos expériences, vos centres d'intérêt..."
                      className="min-h-[120px] rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20 resize-none"
                    />
                  </div>

                  {isEditing && (
                    <div className="flex justify-end gap-3 mt-8 pt-6 border-t border-gray-100">
                      <Button
                        variant="outline"
                        onClick={() => setIsEditing(false)}
                        className="px-6 py-2.5 rounded-xl"
                      >
                        Annuler
                      </Button>
                      <Button
                        onClick={handleSaveProfile}
                        className="bg-[#15803D] hover:bg-[#166534] px-6 py-2.5 rounded-xl"
                      >
                        <Save className="w-4 h-4 mr-2" />
                        Sauvegarder
                      </Button>
                    </div>
                  )}
                </Card>

                {/* Statistiques personnelles améliorées */}
                <Card className="p-8 shadow-sm border-0 bg-white/80 backdrop-blur-sm rounded-2xl">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-blue-50 rounded-xl">
                      <TrendingUp className="w-5 h-5 text-blue-600" />
                    </div>
                    <h3 className="text-xl font-semibold text-gray-900">Statistiques</h3>
                  </div>
                  
                  <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
                    <div className="text-center p-6 bg-gradient-to-br from-blue-50 to-blue-100 rounded-2xl border border-blue-100">
                      <div className="flex items-center justify-center w-12 h-12 bg-blue-500 rounded-xl mx-auto mb-3">
                        <Calendar className="w-6 h-6 text-white" />
                      </div>
                      <p className="text-2xl font-bold text-blue-600 mb-1">24</p>
                      <p className="text-sm text-gray-600">Cours cette semaine</p>
                    </div>
                    
                    <div className="text-center p-6 bg-gradient-to-br from-green-50 to-green-100 rounded-2xl border border-green-100">
                      <div className="flex items-center justify-center w-12 h-12 bg-green-500 rounded-xl mx-auto mb-3">
                        <Clock className="w-6 h-6 text-white" />
                      </div>
                      <p className="text-2xl font-bold text-green-600 mb-1">156</p>
                      <p className="text-sm text-gray-600">Heures enseignées</p>
                    </div>
                    
                    <div className="text-center p-6 bg-gradient-to-br from-purple-50 to-purple-100 rounded-2xl border border-purple-100">
                      <div className="flex items-center justify-center w-12 h-12 bg-purple-500 rounded-xl mx-auto mb-3">
                        <MapPin className="w-6 h-6 text-white" />
                      </div>
                      <p className="text-2xl font-bold text-purple-600 mb-1">8</p>
                      <p className="text-sm text-gray-600">Salles utilisées</p>
                    </div>
                    
                    <div className="text-center p-6 bg-gradient-to-br from-orange-50 to-orange-100 rounded-2xl border border-orange-100">
                      <div className="flex items-center justify-center w-12 h-12 bg-orange-500 rounded-xl mx-auto mb-3">
                        <Award className="w-6 h-6 text-white" />
                      </div>
                      <p className="text-2xl font-bold text-orange-600 mb-1">95%</p>
                      <p className="text-sm text-gray-600">Taux de présence</p>
                    </div>
                  </div>
                </Card>
              </div>

              {/* Colonne latérale */}
              <div className="space-y-6">
                
                {/* Sécurité */}
                <Card className="p-6 shadow-sm border-0 bg-white/80 backdrop-blur-sm rounded-2xl">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-red-50 rounded-xl">
                      <Shield className="w-5 h-5 text-red-600" />
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900">Sécurité</h3>
                  </div>
                  
                  {!isChangingPassword ? (
                    <div className="space-y-4">
                      <div className="p-4 bg-gray-50 rounded-xl">
                        <p className="text-sm text-gray-600 mb-2">
                          <strong>Dernière modification :</strong>
                        </p>
                        <p className="text-sm text-gray-500">Il y a 30 jours</p>
                      </div>
                      <Button
                        onClick={() => setIsChangingPassword(true)}
                        variant="outline"
                        className="w-full h-12 rounded-xl border-gray-200 hover:bg-gray-50"
                      >
                        <Key className="w-4 h-4 mr-2" />
                        Changer le mot de passe
                      </Button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <div>
                        <Label htmlFor="currentPassword" className="text-sm font-medium text-gray-700 mb-2 block">
                          Mot de passe actuel
                        </Label>
                        <div className="relative">
                          <Input
                            id="currentPassword"
                            type={showCurrentPassword ? "text" : "password"}
                            value={passwordData.currentPassword}
                            onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})}
                            className="pr-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                          <button
                            type="button"
                            onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                          >
                            {showCurrentPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                          </button>
                        </div>
                      </div>
                      
                      <div>
                        <Label htmlFor="newPassword" className="text-sm font-medium text-gray-700 mb-2 block">
                          Nouveau mot de passe
                        </Label>
                        <div className="relative">
                          <Input
                            id="newPassword"
                            type={showNewPassword ? "text" : "password"}
                            value={passwordData.newPassword}
                            onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
                            className="pr-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                          <button
                            type="button"
                            onClick={() => setShowNewPassword(!showNewPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                          >
                            {showNewPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                          </button>
                        </div>
                      </div>
                      
                      <div>
                        <Label htmlFor="confirmPassword" className="text-sm font-medium text-gray-700 mb-2 block">
                          Confirmer le mot de passe
                        </Label>
                        <div className="relative">
                          <Input
                            id="confirmPassword"
                            type={showConfirmPassword ? "text" : "password"}
                            value={passwordData.confirmPassword}
                            onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                            className="pr-10 h-12 rounded-xl border-gray-200 focus:border-[#15803D] focus:ring-[#15803D]/20"
                          />
                          <button
                            type="button"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                          >
                            {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                          </button>
                        </div>
                      </div>
                      
                      <div className="flex flex-col gap-2 pt-2">
                        <Button
                          onClick={handleChangePassword}
                          className="w-full bg-[#15803D] hover:bg-[#166534] h-12 rounded-xl"
                        >
                          <Save className="w-4 h-4 mr-2" />
                          Confirmer
                        </Button>
                        <Button
                          variant="outline"
                          onClick={() => {
                            setIsChangingPassword(false)
                            setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" })
                          }}
                          className="w-full h-12 rounded-xl border-gray-200"
                        >
                          Annuler
                        </Button>
                      </div>
                    </div>
                  )}
                </Card>

                {/* Préférences */}
                <Card className="p-6 shadow-sm border-0 bg-white/80 backdrop-blur-sm rounded-2xl">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="p-2 bg-purple-50 rounded-xl">
                      <Bell className="w-5 h-5 text-purple-600" />
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900">Préférences</h3>
                  </div>
                  
                  <div className="space-y-6">
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900 mb-1">Notifications email</p>
                        <p className="text-sm text-gray-600">Recevoir les notifications par email</p>
                      </div>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={preferences.emailNotifications}
                          onChange={(e) => setPreferences({...preferences, emailNotifications: e.target.checked})}
                          className="sr-only peer"
                        />
                        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-[#15803D]/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#15803D]"></div>
                      </label>
                    </div>
                    
                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900 mb-1">Notifications push</p>
                        <p className="text-sm text-gray-600">Recevoir les notifications dans le navigateur</p>
                      </div>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={preferences.pushNotifications}
                          onChange={(e) => setPreferences({...preferences, pushNotifications: e.target.checked})}
                          className="sr-only peer"
                        />
                        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-[#15803D]/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#15803D]"></div>
                      </label>
                    </div>
                    
                    <div className="p-4 bg-gray-50 rounded-xl">
                      <Label htmlFor="language" className="flex items-center gap-2 font-medium text-gray-900 mb-3">
                        <Globe className="w-4 h-4" />
                        Langue
                      </Label>
                      <select
                        id="language"
                        value={preferences.language}
                        onChange={(e) => setPreferences({...preferences, language: e.target.value})}
                        className="w-full p-3 border border-gray-200 rounded-xl focus:ring-[#15803D] focus:border-[#15803D] bg-white"
                      >
                        <option value="fr">🇫🇷 Français</option>
                        <option value="en">🇺🇸 English</option>
                      </select>
                    </div>
                  </div>
                </Card>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}