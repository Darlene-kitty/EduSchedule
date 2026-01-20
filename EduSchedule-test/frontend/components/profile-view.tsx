"use client"

import type React from "react"
import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
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
  Globe
} from "lucide-react"

export function ProfileView() {
  const { user } = useAuth()
  const { toast } = useToast()
  const [isEditing, setIsEditing] = useState(false)
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  
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
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="profile" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header 
          title="Mon Profil" 
          subtitle="Gérez vos informations personnelles et préférences" 
        />

        <main className="flex-1 overflow-y-auto p-6">
          <div className="max-w-4xl mx-auto space-y-6">
            
            {/* Carte principale du profil */}
            <Card className="p-6">
              <div className="flex items-start justify-between mb-6">
                <div className="flex items-center gap-4">
                  <div className="relative">
                    <img 
                      src={user?.avatar || "/placeholder.svg"} 
                      alt={user?.name} 
                      className="w-20 h-20 rounded-full object-cover"
                    />
                    <button className="absolute bottom-0 right-0 bg-[#15803D] text-white p-2 rounded-full hover:bg-[#166534] transition-colors">
                      <Camera className="w-4 h-4" />
                    </button>
                  </div>
                  <div>
                    <h2 className="text-2xl font-bold text-gray-900">{user?.name}</h2>
                    <div className="flex items-center gap-2 mt-1">
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${getRoleBadgeColor(user?.role || "")}`}>
                        {getRoleDisplayName(user?.role || "")}
                      </span>
                    </div>
                    <p className="text-gray-600 mt-2 flex items-center gap-2">
                      <Mail className="w-4 h-4" />
                      {user?.email}
                    </p>
                  </div>
                </div>
                
                <Button
                  onClick={() => setIsEditing(!isEditing)}
                  variant={isEditing ? "outline" : "default"}
                  className={isEditing ? "" : "bg-[#15803D] hover:bg-[#166534]"}
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
              </div>
              {/* Informations personnelles */}
              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="name">Nom complet</Label>
                    <Input
                      id="name"
                      value={profileData.name}
                      onChange={(e) => setProfileData({...profileData, name: e.target.value})}
                      disabled={!isEditing}
                      className="mt-1"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      value={profileData.email}
                      onChange={(e) => setProfileData({...profileData, email: e.target.value})}
                      disabled={!isEditing}
                      className="mt-1"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="phone">Téléphone</Label>
                    <Input
                      id="phone"
                      value={profileData.phone}
                      onChange={(e) => setProfileData({...profileData, phone: e.target.value})}
                      disabled={!isEditing}
                      placeholder="+237 6XX XXX XXX"
                      className="mt-1"
                    />
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <Label htmlFor="department">Département</Label>
                    <Input
                      id="department"
                      value={profileData.department}
                      onChange={(e) => setProfileData({...profileData, department: e.target.value})}
                      disabled={!isEditing}
                      placeholder="Ex: Informatique, Mathématiques..."
                      className="mt-1"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="specialization">Spécialisation</Label>
                    <Input
                      id="specialization"
                      value={profileData.specialization}
                      onChange={(e) => setProfileData({...profileData, specialization: e.target.value})}
                      disabled={!isEditing}
                      placeholder="Ex: Développement Web, Algèbre..."
                      className="mt-1"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="address">Adresse</Label>
                    <Input
                      id="address"
                      value={profileData.address}
                      onChange={(e) => setProfileData({...profileData, address: e.target.value})}
                      disabled={!isEditing}
                      placeholder="Votre adresse"
                      className="mt-1"
                    />
                  </div>
                </div>
              </div>

              <div className="mt-6">
                <Label htmlFor="bio">Biographie</Label>
                <Textarea
                  id="bio"
                  value={profileData.bio}
                  onChange={(e) => setProfileData({...profileData, bio: e.target.value})}
                  disabled={!isEditing}
                  placeholder="Parlez-nous de vous..."
                  className="mt-1 min-h-[100px]"
                />
              </div>

              {isEditing && (
                <div className="flex justify-end gap-3 mt-6 pt-6 border-t">
                  <Button
                    variant="outline"
                    onClick={() => setIsEditing(false)}
                  >
                    Annuler
                  </Button>
                  <Button
                    onClick={handleSaveProfile}
                    className="bg-[#15803D] hover:bg-[#166534]"
                  >
                    <Save className="w-4 h-4 mr-2" />
                    Sauvegarder
                  </Button>
                </div>
              )}
            </Card>

            <div className="grid md:grid-cols-2 gap-6">
              
              {/* Sécurité */}
              <Card className="p-6">
                <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                  <Key className="w-5 h-5" />
                  Sécurité
                </h3>
                
                {!isChangingPassword ? (
                  <div className="space-y-4">
                    <p className="text-gray-600 text-sm">
                      Dernière modification du mot de passe : Il y a 30 jours
                    </p>
                    <Button
                      onClick={() => setIsChangingPassword(true)}
                      variant="outline"
                      className="w-full"
                    >
                      Changer le mot de passe
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="currentPassword">Mot de passe actuel</Label>
                      <Input
                        id="currentPassword"
                        type="password"
                        value={passwordData.currentPassword}
                        onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})}
                        className="mt-1"
                      />
                    </div>
                    
                    <div>
                      <Label htmlFor="newPassword">Nouveau mot de passe</Label>
                      <Input
                        id="newPassword"
                        type="password"
                        value={passwordData.newPassword}
                        onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
                        className="mt-1"
                      />
                    </div>
                    
                    <div>
                      <Label htmlFor="confirmPassword">Confirmer le mot de passe</Label>
                      <Input
                        id="confirmPassword"
                        type="password"
                        value={passwordData.confirmPassword}
                        onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                        className="mt-1"
                      />
                    </div>
                    
                    <div className="flex gap-2">
                      <Button
                        onClick={handleChangePassword}
                        className="flex-1 bg-[#15803D] hover:bg-[#166534]"
                      >
                        Confirmer
                      </Button>
                      <Button
                        variant="outline"
                        onClick={() => setIsChangingPassword(false)}
                        className="flex-1"
                      >
                        Annuler
                      </Button>
                    </div>
                  </div>
                )}
              </Card>

              {/* Préférences */}
              <Card className="p-6">
                <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                  <Bell className="w-5 h-5" />
                  Préférences
                </h3>
                
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">Notifications email</p>
                      <p className="text-sm text-gray-600">Recevoir les notifications par email</p>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.emailNotifications}
                      onChange={(e) => setPreferences({...preferences, emailNotifications: e.target.checked})}
                      className="h-4 w-4 text-[#15803D] focus:ring-[#15803D] border-gray-300 rounded"
                    />
                  </div>
                  
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium">Notifications push</p>
                      <p className="text-sm text-gray-600">Recevoir les notifications dans le navigateur</p>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.pushNotifications}
                      onChange={(e) => setPreferences({...preferences, pushNotifications: e.target.checked})}
                      className="h-4 w-4 text-[#15803D] focus:ring-[#15803D] border-gray-300 rounded"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="language" className="flex items-center gap-2">
                      <Globe className="w-4 h-4" />
                      Langue
                    </Label>
                    <select
                      id="language"
                      value={preferences.language}
                      onChange={(e) => setPreferences({...preferences, language: e.target.value})}
                      className="mt-1 w-full p-2 border border-gray-300 rounded-md focus:ring-[#15803D] focus:border-[#15803D]"
                    >
                      <option value="fr">Français</option>
                      <option value="en">English</option>
                    </select>
                  </div>
                </div>
              </Card>
            </div>

            {/* Statistiques personnelles */}
            <Card className="p-6">
              <h3 className="text-lg font-semibold mb-4">Statistiques</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="text-center p-4 bg-blue-50 rounded-lg">
                  <p className="text-2xl font-bold text-blue-600">24</p>
                  <p className="text-sm text-gray-600">Cours cette semaine</p>
                </div>
                <div className="text-center p-4 bg-green-50 rounded-lg">
                  <p className="text-2xl font-bold text-green-600">156</p>
                  <p className="text-sm text-gray-600">Heures enseignées</p>
                </div>
                <div className="text-center p-4 bg-purple-50 rounded-lg">
                  <p className="text-2xl font-bold text-purple-600">8</p>
                  <p className="text-sm text-gray-600">Salles utilisées</p>
                </div>
                <div className="text-center p-4 bg-orange-50 rounded-lg">
                  <p className="text-2xl font-bold text-orange-600">95%</p>
                  <p className="text-sm text-gray-600">Taux de présence</p>
                </div>
              </div>
            </Card>
          </div>
        </main>
      </div>
    </div>
  )
}