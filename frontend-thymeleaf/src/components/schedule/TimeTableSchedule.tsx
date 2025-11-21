import React, { useState } from 'react';
import { Calendar, Menu, Bell, Plus, Clock, Edit2, Copy, Trash2, ExternalLink } from 'lucide-react';

interface Course {
  id: number;
  day: string;
  title: string;
  code: string;
  level: string;
  teacher: string;
  time: string;
  color: string;
}

export default function TimeTableScheduling() {
  const [courses, setCourses] = useState<Course[]>([
  {
    id: 1,
    day: 'Lundi',
    title: 'Mathématiques',
    code: 'A101',
    level: 'L1-G1',
    teacher: 'Dr. Martin',
    time: '08:00-10:00',
    color: 'bg-blue-500'
  },
    {
      id: 2,
      day: 'Lundi',
      title: 'Algèbre',
      code: 'A102',
      level: 'L2-G2',
      teacher: 'Prof. Dubois',
      time: '14:00-16:00',
      color: 'bg-pink-500'
    },
    {
      id: 3,
      day: 'Mardi',
      title: 'Physique',
      code: 'B203',
      level: 'L1-G1',
      teacher: 'Dr. Laurent',
      time: '10:00-12:00',
      color: 'bg-green-500'
    },
    {
      id: 4,
      day: 'Mardi',
      title: 'TP Physique',
      code: 'Lab B1',
      level: 'L1-G2',
      teacher: 'Dr. Laurent',
      time: '14:00-16:00',
      color: 'bg-green-500'
    },
    {
      id: 5,
      day: 'Mercredi',
      title: 'Analyse',
      code: 'A103',
      level: 'L2-G1',
      teacher: 'Prof. Bernard',
      time: '08:00-10:00',
      color: 'bg-cyan-500'
    },
    {
      id: 6,
      day: 'Jeudi',
      title: 'Géométrie',
      code: 'A101',
      level: 'L1-G1',
      teacher: 'Dr. Martin',
      time: '09:00-11:00',
      color: 'bg-blue-600'
    },
    {
      id: 7,
      day: 'Jeudi',
      title: 'Séminaire',
      code: 'Amphi A',
      level: 'M1',
      teacher: 'Prof. Richard',
      time: '14:00-17:00',
      color: 'bg-orange-500'
    },
    {
      id: 8,
      day: 'Vendredi',
      title: 'Statistiques',
      code: 'A104',
      level: 'L3-G1',
      teacher: 'Dr. Sophie',
      time: '08:00-10:00',
      color: 'bg-red-500'
    },
    {
      id: 9,
      day: 'Vendredi',
      title: 'Probabilités',
      code: 'A104',
      level: 'L3-G2',
      teacher: 'Dr. Sophie',
      time: '10:00-12:00',
      color: 'bg-red-500'
    }
  ]);

  const [draggedCourse, setDraggedCourse] = useState<Course | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  const days = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'];

  const handleDragStart = (e: React.DragEvent<HTMLDivElement>, course: Course) => {
  setDraggedCourse(course);
  e.currentTarget.style.opacity = '0.5';
};

const handleDragEnd = (e: React.DragEvent<HTMLDivElement>) => {
  e.currentTarget.style.opacity = '1';
  setDraggedCourse(null);
};

const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
  e.preventDefault();
};

const handleDrop = (e: React.DragEvent<HTMLDivElement>, targetDay: string) => {
  e.preventDefault();
  if (draggedCourse && draggedCourse.day !== targetDay) {
    setCourses(courses.map(course => 
      course.id === draggedCourse.id 
        ? { ...course, day: targetDay }
        : course
    ));
  }
};

  const getCoursesForDay = (day: string): Course[] => {
  return courses.filter(course => course.day === day);
};

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="flex items-center justify-between px-6 py-4">
          <div className="flex items-center gap-4">
            <button className="lg:hidden">
              <Menu className="w-6 h-6 text-gray-600" />
            </button>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-yellow-400 rounded-xl flex items-center justify-center">
                <Calendar className="w-6 h-6 text-slate-900" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">EduSchedule</h1>
                <p className="text-xs text-gray-500">Gestion intelligente</p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <span className="text-sm text-gray-600">Samedi 18 Octobre 2025</span>
            <span className="text-sm font-medium text-gray-900">09:30</span>
            <div className="relative">
              <Bell className="w-5 h-5 text-gray-600" />
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">2</span>
            </div>
            <div className="flex items-center gap-2 ml-2">
              <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-medium">
                AS
              </div>
              <span className="text-sm font-medium text-gray-900 hidden sm:block">Admin</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="p-6">
        {/* Page Header */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-2xl font-bold text-gray-900">Planification des emplois du temps</h2>
            <button 
              onClick={() => setShowAddModal(true)}
              className="flex items-center gap-2 bg-green-500 text-white px-4 py-2 rounded-lg hover:bg-green-600 transition-colors"
            >
              <Plus className="w-5 h-5" />
              Nouveau cours
            </button>
          </div>
          <p className="text-gray-600">Glissez-déposez les cours pour les réorganiser</p>
        </div>

        {/* Info Card */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <h3 className="font-semibold text-gray-900 mb-2">Emploi du temps hebdomadaire</h3>
          <p className="text-sm text-gray-700">Glissez et déposez les cours pour les déplacer entre les jours</p>
        </div>

        {/* Timetable Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
          {days.map(day => (
            <div 
              key={day}
              className="bg-white rounded-lg shadow-sm border border-gray-200"
              onDragOver={handleDragOver}
              onDrop={(e) => handleDrop(e, day)}
            >
              {/* Day Header */}
              <div className="bg-gray-800 text-white px-4 py-3 rounded-t-lg">
                <h3 className="font-semibold text-center">{day}</h3>
              </div>

              {/* Courses */}
              <div className="p-3 space-y-3 min-h-[400px]">
                {getCoursesForDay(day).map(course => (
                  <div
                    key={course.id}
                    draggable
                    onDragStart={(e) => handleDragStart(e, course)}
                    onDragEnd={handleDragEnd}
                    className={`${course.color} rounded-lg p-4 text-white cursor-move hover:shadow-lg transition-shadow`}
                  >
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Clock className="w-4 h-4" />
                        <span className="text-sm font-medium">{course.time}</span>
                      </div>
                      <span className="text-xs bg-white bg-opacity-20 px-2 py-1 rounded">0</span>
                    </div>

                    <h4 className="font-bold text-lg mb-1">{course.title}</h4>
                    <div className="space-y-1 text-sm">
                      <div className="bg-white bg-opacity-20 px-2 py-1 rounded inline-block">
                        {course.code}
                      </div>
                      <div className="bg-white bg-opacity-20 px-2 py-1 rounded inline-block ml-1">
                        {course.level}
                      </div>
                    </div>
                    
                    <div className="mt-3 pt-3 border-t border-white border-opacity-30">
                      <p className="font-medium">{course.teacher}</p>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center gap-2 mt-3">
                      <button className="p-1 hover:bg-white hover:bg-opacity-20 rounded">
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button className="p-1 hover:bg-white hover:bg-opacity-20 rounded">
                        <Copy className="w-4 h-4" />
                      </button>
                      <button className="p-1 hover:bg-white hover:bg-opacity-20 rounded">
                        <ExternalLink className="w-4 h-4" />
                      </button>
                      <button className="p-1 hover:bg-white hover:bg-opacity-20 rounded">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </main>

      {/* Add Course Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-900 mb-4">Ajouter un nouveau cours</h3>
            <div className="space-y-4">
              <input
                type="text"
                placeholder="Titre du cours"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
              <input
                type="text"
                placeholder="Code du cours"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
              <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="">Sélectionner un jour</option>
                {days.map(day => (
                  <option key={day} value={day}>{day}</option>
                ))}
              </select>
              <input
                type="text"
                placeholder="Horaire (ex: 08:00-10:00)"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
              <input
                type="text"
                placeholder="Enseignant"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
              <div className="flex gap-3 mt-6">
                <button
                  onClick={() => setShowAddModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Annuler
                </button>
                <button
                  onClick={() => {
                    setShowAddModal(false);
                    alert('Fonctionnalité d\'ajout à implémenter');
                  }}
                  className="flex-1 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600"
                >
                  Ajouter
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}